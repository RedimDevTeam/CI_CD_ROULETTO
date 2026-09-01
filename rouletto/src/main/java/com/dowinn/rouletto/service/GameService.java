package com.dowinn.rouletto.service;

import com.dowinn.rouletto.communication.APIResponse;
import com.dowinn.rouletto.communication.PlayerCommunication;
import com.dowinn.rouletto.communication.StatusCode;
import com.dowinn.rouletto.dto.api.request.GameCancelRequestDto;
import com.dowinn.rouletto.dto.api.request.GameDealRequestDto;
import com.dowinn.rouletto.dto.api.request.GameStartRequestDto;
import com.dowinn.rouletto.dto.socket.response.BetTimerData;
import com.dowinn.rouletto.dto.socket.response.CancelData;
import com.dowinn.rouletto.dto.socket.response.PastResultDto;
import com.dowinn.rouletto.entity.Game;
import com.dowinn.rouletto.entity.TableConfig;
import com.dowinn.rouletto.enums.GameStatus;
import com.dowinn.rouletto.enums.Timers;
import com.dowinn.rouletto.logic.GameLogic;
import com.dowinn.rouletto.model.GameDetail;
import com.dowinn.rouletto.redis.RedisHelper;
import com.dowinn.rouletto.repository.GameRepository;
import com.dowinn.rouletto.repository.TableConfigRepository;
import com.dowinn.rouletto.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class GameService {

    @Autowired
    GameRepository gameRepository;

    @Autowired
    GameLogic gameLogic;

    @Autowired
    PayOffService payOffService;

    @Autowired
    TableConfigRepository tableConfigRepository;

    @Autowired
    RedisHelper redisHelper;

    @Autowired
    HistoryService historyService;
    
    @Autowired
    PlayerCommunication playerCommunication;

    @Autowired
    RecordingService recordingService;

    private static ConcurrentHashMap<String, GameDetail> gameDetails = new ConcurrentHashMap<>();

    public GameDetail startGame(GameStartRequestDto gameStartRequestDto) {
        log.info("game start request {}", gameStartRequestDto);
        GameDetail gameDetail = initGame(gameStartRequestDto.getTableId(), gameStartRequestDto.getMachineGameId(), gameStartRequestDto.getMachineId());
        return gameDetail;
    }


    private GameDetail initGame(String tableId, String machineGameId, String machineId) {
        //check for pending games
        try {
            checkPendingGame(tableId, machineGameId, machineId);

            GameDetail gameDetail = gameDetails.get(tableId);
            log.info("gameDetail {}",gameDetail);
            //implement redis for timers
            if (gameDetail != null) {
                gameDetails.remove(tableId);
            }
            gameDetail = createNewGame(tableId, machineGameId, machineId);

            TableConfig tableConfig = redisHelper.getTableConfig(tableId);
            if(tableConfig==null) {
                tableConfig = tableConfigRepository.findByTableIdAndActive(tableId, true);
                redisHelper.setTableConfig(tableId,tableConfig);
            }

            if (tableConfig.getTimers() != null) {
                gameDetail = Timers.setTimers(gameDetail, tableConfig.getTimers());
            }
            gameDetails.put(tableId, gameDetail);
            log.info("game details list {}",gameDetails);
            sendBetTimer(gameDetail);
            //   recordingService.executeRemoteScript(gameDetail,"start",false);

            return gameDetail;
        } catch (Exception e) {
            log.info("exception e {}", e);
        }
        return null;

    }

    public void spinBallRecord(GameStartRequestDto gameStartRequestDto){
        GameDetail gameDetail = gameDetails.get(gameStartRequestDto.getTableId());
        recordingService.executeRemoteScript(gameDetail,"start",false);
    }

    public APIResponse dealBall(GameDealRequestDto gameDealRequestDto) {
        log.info("dealer request {}", gameDealRequestDto);

        GameDetail gameDetail = gameDetails.get(gameDealRequestDto.getTableId());
        if(gameDetail==null){
            return APIResponse.get(StatusCode.NO_GAME_STARTED);
        }
        log.info("gameDetail {}",gameDetail);
        //todo in future validate using DI inputs
        Optional<Game> gameOptional = gameRepository.findById(gameDetail.getGameId());

        if (gameOptional.isEmpty()) {
            return  APIResponse.get(StatusCode.NO_GAME_STARTED);
        }
        Game game = gameOptional.get();
        if (!game.getStatus().equals(GameStatus.INPROGRESS.getStatus())) {
            return  APIResponse.get(StatusCode.IN_PROGRESS);
        }

        gameDetail.setBalls(gameDealRequestDto.getBalls());
        gameDetail.setEnd(LocalDateTime.now());

        boolean result = gameLogic.checkResult(gameDetail);
        log.info("result {}", result);
        if (result) {
            payOffService.initiatePayOff(gameDetail);
        }

        historyService.saveGameResult(gameDetail, GameStatus.COMPLETED);
        updateGameStatus(game,GameStatus.COMPLETED);

        publishLastResults(gameDetail.getTableId());
        recordingService.executeRemoteScript(gameDetail,"stop",false);
        gameDetails.remove(gameDetail.getTableId());

      return APIResponse.get(StatusCode.GAME_COMPLETED);
    }

    private void publishLastResults(String tableId) {
        PastResultDto pastResultStats = historyService.getPastResultStats(tableId,false);
        playerCommunication.sendTableMessage(pastResultStats,StatusCode.WS_ROULETTE_RESULT,tableId);
    }

    public void cancelGame(GameCancelRequestDto gameCancelRequestDto,Boolean isBoCancel) {
    log.info("game cancel called {}",gameCancelRequestDto);
        if(isBoCancel) {
            Optional<Game> opt = gameRepository.findById(gameCancelRequestDto.getGameId());
            if(opt.isPresent()){
                Game game = opt.get();
                game.setStatus(GameStatus.CANCEL.getStatus());
                gameRepository.save(game);
            }
        }
        GameDetail gameDetail = new GameDetail();
        gameDetail.setGameId(gameCancelRequestDto.getGameId());
        gameDetail.setTableId(gameCancelRequestDto.getTableId());
        gameDetail.setStatus(GameStatus.CANCEL);
        playerCommunication.sendTableMessage(new CancelData(gameDetail.getGameId()),StatusCode.GAME_CANCEL,gameDetail.getTableId());
        payOffService.settleCancelledGame(gameDetail);
        recordingService.executeRemoteScript(gameDetail,"stop",false);
        GameDetail activeGame = gameDetails.get(gameCancelRequestDto.getTableId());
        if (activeGame != null && activeGame.getGameId().equals(gameCancelRequestDto.getGameId())) {
            gameDetails.remove(gameCancelRequestDto.getTableId());
        } else {
            log.info("Skipping in-memory game removal for tableId={}, cancel gameId={}, active gameId={}",
                    gameCancelRequestDto.getTableId(),
                    gameCancelRequestDto.getGameId(),
                    activeGame != null ? activeGame.getGameId() : null);
        }
    }

    public void sendBetTimer(String tableId, Long playerId) {
        GameDetail gameDetail = getGameDetail(tableId);
        if (gameDetail != null) {
            BetTimerData betTimer = getBetTimer(gameDetail);
            if (betTimer.getSecs() > 0)
                playerCommunication.sendPlayerMessage(betTimer, StatusCode.BET_TIMER, playerId);
        }
    }

    private void sendBetTimer(GameDetail gameDetail) {
        BetTimerData betTimer = getBetTimer(gameDetail);
        playerCommunication.sendTableMessage(betTimer, StatusCode.BET_TIMER, gameDetail.getTableId());
    }

    public BetTimerData getBetTimer(GameDetail gameDetail) {
        BetTimerData betTimerData = new BetTimerData();
        betTimerData.setGameId(gameDetail.getGameId());
        betTimerData.setSecs(DateTimeUtil.remainingSeconds(gameDetail.getStarts(), gameDetail.getTimers().get(Timers.BET_TIMER)));
        betTimerData.setBaseSecs(gameDetail.getTimers().get(Timers.BET_TIMER));
        betTimerData.setDiff(DateTimeUtil.completedSeconds(gameDetail.getStarts(), gameDetail.getTimers().get(Timers.BET_TIMER)));
        return betTimerData;
    }

    private GameDetail createNewGame(String tableId, String machineGameId, String machineId) {
        String gameId = tableId.substring(0, 3) + "" + (new Date()).getTime();

        GameDetail gameDetail = new GameDetail();
        gameDetail.setGameId(gameId);
        gameDetail.setTableId(tableId);
        gameDetail.setStatus(GameStatus.INPROGRESS);
        gameDetail.setStarts(LocalDateTime.now());
        gameDetail.setMachineGameId(machineGameId);
        gameDetail.setMachineId(machineId);
        // todo need to discuss dealerId ,shoe id
        Game game = new Game();
        game.setId(gameId);
        game.setTableId(tableId);
        game.setNotes(null);
        game.setShoe(null);
        game.setDealerId(null);

        updateGameStatus(game,GameStatus.INPROGRESS);
        return gameDetail;
    }

    private void checkPendingGame(String tableId, String machineGameId, String machineId) {
        Optional<Game> active = gameRepository.findByStatusAndTableId(GameStatus.INPROGRESS.getStatus(), tableId);
        if (active.isPresent()) {
            log.info("game cancel called for game id {}", active.get());
            GameCancelRequestDto gameCancelRequestDto = new GameCancelRequestDto(active.get().getId(), tableId, "cancelled");
            cancelGame(gameCancelRequestDto, false);
            Game game = active.get();
            updateGameStatus(game, GameStatus.CANCEL);
        }
    }


    public GameDetail getGameDetail(String tableId) {
        return gameDetails.get(tableId);
    }

    private void updateGameStatus(Game game, GameStatus gameStatus) {
        game.setStatus(gameStatus.getStatus());
        gameRepository.save(game);
    }
}

