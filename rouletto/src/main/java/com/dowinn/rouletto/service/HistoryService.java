package com.dowinn.rouletto.service;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.dowinn.rouletto.communication.APIResponse;
import com.dowinn.rouletto.communication.StatusCode;
import com.dowinn.rouletto.dto.api.request.GameHistoryRequestDto;
import com.dowinn.rouletto.dto.api.request.SaveBetRequestDto;
import com.dowinn.rouletto.dto.api.response.GameHistoryResponseDto;
import com.dowinn.rouletto.dto.api.response.SaveBetResponseDto;
import com.dowinn.rouletto.dto.socket.response.PastResultDto;
import com.dowinn.rouletto.entity.GameResults;
import com.dowinn.rouletto.entity.SaveDetails;
import com.dowinn.rouletto.entity.Shoes;
import com.dowinn.rouletto.enums.GameStatus;
import com.dowinn.rouletto.model.GameDetail;
import com.dowinn.rouletto.model.PlayerBetHistory;
import com.dowinn.rouletto.redis.RedisHelper;
import com.dowinn.rouletto.repository.*;
import com.dowinn.rouletto.util.DateTimeUtil;
import com.game.jwt.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Array;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class HistoryService {


    @Autowired
    GameResultRepository gameResultRepository;

    @Autowired
    ShoeRepository shoeRepository;

    @Autowired
    SaveDetailsRepository saveDetailsRepository;

    @Autowired
    RedisHelper redisHelper;

    @Value("${video.config.url}")
    private String videourl;


    @Autowired
    JdbcTemplate jdbcTemplate;

    public PastResultDto getPastResultStats(String tableId,Boolean isSocket) {
        if(isSocket){
            PastResultDto pastResult = redisHelper.getPastResult(tableId);
            if(pastResult!=null){
                return pastResult;
            }
        }

        List<GameResults> pastResults = gameResultRepository.findAllByTableId(tableId, 100);
        if (pastResults.size() <= 0) {
            return null;
        }

        List<PastResultDto.BallData> ballResults = pastResults.stream().map(a -> {
            PastResultDto.BallData ballData = new PastResultDto.BallData();
            ballData.setBalls(a.getBallNumbers());
            ballData.setGameId(a.getGameId());
            return ballData;
        }).collect(Collectors.toList());

        Map<Integer, Long> ballNumberFrequency = pastResults.stream()
                .flatMap(a -> a.getBallNumbers().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<Integer, Long> sortedBallFrequency =
                IntStream.rangeClosed(0, 36)
                        .boxed()
                        .collect(Collectors.toMap(
                                i -> i,
                                i -> ballNumberFrequency.getOrDefault(i, 0L)
                        ))
                        .entrySet()
                        .stream()
                        .sorted(
                                Map.Entry.<Integer, Long>comparingByValue().reversed()
                                        .thenComparing(Map.Entry.comparingByKey()) // optional tie-break
                        )
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (a, b) -> a,
                                LinkedHashMap::new
                        ));


        List<Map.Entry<Integer, Long>> sortedBalls = new LinkedList<>(sortedBallFrequency.entrySet());
        List<Integer> hot = sortedBalls.stream().limit(4).map(Map.Entry::getKey).collect(Collectors.toList());
        List<Long> hotOcc = sortedBalls.stream().limit(4).map(Map.Entry::getValue).collect(Collectors.toList());
        List<Integer> cold = sortedBalls.stream().skip(sortedBalls.size() - 4).map(Map.Entry::getKey).collect(Collectors.toList());
        List<Long> coldOcc = sortedBalls.stream().skip(sortedBalls.size() - 4).map(Map.Entry::getValue).collect(Collectors.toList());

        Map<Boolean, Long> oddEvenCount = pastResults.stream().collect(Collectors.partitioningBy(spot -> spot.getBallNumbers().stream().reduce(0, (a, b) -> a + b) % 2 == 0, Collectors.counting()));
        log.info("odd even count {}", oddEvenCount);
        Long even = oddEvenCount.getOrDefault(true, 0L);
        Long odd = oddEvenCount.getOrDefault(false, 0L);

        log.info("(even {} /pastResults.size() {})*100.0 {}", even, pastResults.size(), (even / pastResults.size()) * 100.0);

        PastResultDto pastResultDto = new PastResultDto();
        pastResultDto.setCOLD_OCC(coldOcc);
        pastResultDto.setCOLD(cold);
        pastResultDto.setHOT_OCC(hotOcc);
        pastResultDto.setHOT(hot);
        pastResultDto.setEVEN(BigDecimal.valueOf(((double) even / (double) pastResults.size()) * 100.0).setScale(2, RoundingMode.CEILING));
        pastResultDto.setODD(BigDecimal.valueOf(((double) odd / (double) pastResults.size()) * 100.0).setScale(2, RoundingMode.CEILING));
        pastResultDto.setSPOT(new ArrayList<>(sortedBallFrequency.keySet()));
        pastResultDto.setSPOT_OCC(new ArrayList<>(sortedBallFrequency.values()));
        pastResultDto.setLAST_GAME_RESULT(ballResults);
        redisHelper.setPastResult(tableId,pastResultDto);
        return pastResultDto;
    }

    private Shoes createShoes(String tableId) {
        Shoes shoes = new Shoes();
        shoes.setActive(true);
        shoes.setTableId(tableId);
        shoes.setStartTime(LocalDateTime.now());
        return shoeRepository.save(shoes);
    }

    public GameResults saveGameResult(GameDetail gameDetail, GameStatus gameStatus) {
        String winningspot = gameDetail.getGameResult().getWinningSpot().stream().map(String::valueOf).collect(Collectors.joining(","));
        GameResults gameResults = new GameResults();
        gameResults.setGameId(gameDetail.getGameId());
        gameResults.setTableId(gameDetail.getTableId());
        gameResults.setStatusId(gameStatus.getStatus());
        gameResults.setWinSpots(winningspot);
        gameResults.setBallNumbers(gameDetail.getBalls());
        String date = DateTimeUtil.getDate(gameDetail.getStarts());
        log.info("Date {}",date);
        gameResults.setUrl(String.format(videourl,date,gameDetail.getGameId(),"gameplay"));
        //gameResults.setShoeId(shoes.getId());//todo check with shoe id
        return gameResultRepository.save(gameResults);
    }


    public APIResponse getPlayerHistory(GameHistoryRequestDto gameHistoryRequestDto) {
        String query = "select * from roulette.playerbethistory(?, ?, ?, ?)";
        List<PlayerBetHistory> playerBetHistories = jdbcTemplate.query(query,
                new Object[]{DateTimeUtil.parseDate(gameHistoryRequestDto.getFrom())
                        , DateTimeUtil.parseEndDate(gameHistoryRequestDto.getTo())
                        , gameHistoryRequestDto.getPlayerId(),
                        gameHistoryRequestDto.getTableId()},
                (rs, s) -> {
                    PlayerBetHistory obj = new PlayerBetHistory();
                    obj.setGameId(rs.getString("gameid"));
                    obj.setAmount(rs.getBigDecimal("amount"));
                    obj.setPayoff(rs.getBigDecimal("payoff"));
                    obj.setStatus(rs.getInt("status"));
                    obj.setBetId(rs.getLong("betid"));
                    obj.setWinSpots(rs.getString("winspots"));
                    obj.setGameon(rs.getTimestamp("gameon").toLocalDateTime());
                    obj.setGameType(rs.getInt("gametype"));
                    obj.setGameUrl(rs.getString("url"));
                    Array array = rs.getArray("results");
                    if (array != null) {
                        Integer[] arr = (Integer[]) array.getArray();
                        obj.setResults(Arrays.asList(arr));
                    }
                    Array spotArr = rs.getArray("spot");
                    if (spotArr != null) {
                        Integer[] spot = (Integer[]) spotArr.getArray();
                        obj.setSpot(Arrays.asList(spot));
                    }
                    return obj;
                });
        //  log.info("sql player bet history {}",playerBetHistories);
        Map<String, List<PlayerBetHistory>> playerBetsViaGame = playerBetHistories.stream().collect(Collectors.groupingBy(PlayerBetHistory::getGameId));
        List<GameHistoryResponseDto> gameHistoryResponseDtos = new LinkedList<>();
        for (String games : playerBetsViaGame.keySet()) {
            GameHistoryResponseDto gameHistoryResponseDto = new GameHistoryResponseDto();


            List<PlayerBetHistory> playerBetHistory = playerBetsViaGame.get(games);

            List<Integer> ballNumber = playerBetHistory.get(0).getResults();
            LocalDateTime gameon = playerBetHistory.get(0).getGameon();
            String winSpots = playerBetHistory.get(0).getWinSpots();
            Integer status = playerBetHistory.get(0).getStatus();

            BigDecimal totalBetAmount = playerBetHistory.stream().reduce(BigDecimal.ZERO, (a, b) -> a.add(b.getAmount()), BigDecimal::add);
            BigDecimal totalWinAmount = playerBetHistory.stream().filter(a -> a.getPayoff().compareTo(BigDecimal.ZERO) > 0).reduce(BigDecimal.ZERO, (sum, bet) -> sum.add(bet.getPayoff()), BigDecimal::add);
            // BigDecimal totalWinAmount = playerBetHistory.stream().reduce(BigDecimal.ZERO, (a, b) -> a.add(b.getPayoff()), BigDecimal::add);
            List<GameHistoryResponseDto.BetSpotDetails> mainBets = playerBetHistory.stream()
                    .filter(a -> a.getGameType().equals(1))
                    .map(a -> {
                        GameHistoryResponseDto.BetSpotDetails betSpotDetails = new GameHistoryResponseDto.BetSpotDetails();
                       // log.info("a.getbet spot {}", a.getSpot().get(0));
                        betSpotDetails.setId(a.getSpot().get(0));
                        betSpotDetails.setSpotName(BetSpotService.getBetSpot(a.getSpot().get(0)).getValue());
                        betSpotDetails.setStake(a.getAmount());
                        betSpotDetails.setPayOff(a.getPayoff());
                        return betSpotDetails;
                    }).collect(Collectors.toList());


            List<GameHistoryResponseDto.BetSpotDetails> sideBets = playerBetHistory.stream()
                    .filter(a -> a.getGameType().equals(2))
                    .map(a -> {
                        GameHistoryResponseDto.BetSpotDetails betSpotDetails = new GameHistoryResponseDto.BetSpotDetails();
                      //  log.info("a.getbet spot {}", a.getSpot().get(0));
                        betSpotDetails.setId(a.getSpot().get(0));
                        betSpotDetails.setSpotName(BetSpotService.getBetSpot(a.getSpot().get(0)).getValue());
                        betSpotDetails.setStake(a.getAmount());
                        betSpotDetails.setPayOff(a.getPayoff());
                        return betSpotDetails;
                    }).collect(Collectors.toList());

            List<GameHistoryResponseDto.JackPotDetails> jackPotBets = playerBetHistory.stream()
                    .filter(a -> a.getGameType().equals(3))
                    .map(a -> {
                        GameHistoryResponseDto.JackPotDetails betSpotDetails = new GameHistoryResponseDto.JackPotDetails();
                        betSpotDetails.setId(a.getBetId());
                        betSpotDetails.setBallNumbers(a.getSpot());
                        betSpotDetails.setStake(a.getAmount());
                        betSpotDetails.setPayOff(a.getPayoff());
                        return betSpotDetails;
                    }).collect(Collectors.toList());
            List<String> gameWinspot=new LinkedList<>();
            if(winSpots!=null) {
                gameWinspot = Arrays.stream(winSpots.split(",")).map(a -> BetSpotService.getBetSpot(Integer.valueOf(a)).getValue()).collect(Collectors.toList());
            }

            gameHistoryResponseDto.setMainbet(mainBets);
            gameHistoryResponseDto.setSideBet(sideBets);
            gameHistoryResponseDto.setJackPotBet(jackPotBets);
            gameHistoryResponseDto.setGameOn(gameon);
            gameHistoryResponseDto.setGameId(games);
            gameHistoryResponseDto.setBallNumbers(ballNumber);
            gameHistoryResponseDto.setTotalBetAmount(totalBetAmount);
            gameHistoryResponseDto.setTotalWinAmount(totalWinAmount);
            gameHistoryResponseDto.setWinSpots(gameWinspot);
            gameHistoryResponseDto.setGameUrl(playerBetHistory.get(0).getGameUrl());
            gameHistoryResponseDtos.add(gameHistoryResponseDto);
            gameHistoryResponseDto.setStatus(GameStatus.getValue(status).name());

        }
     //   log.info("game history {}", gameHistoryResponseDtos);
        APIResponse apiResponse = APIResponse.get(StatusCode.SUCCESS, gameHistoryResponseDtos);
        return apiResponse;
    }

    public APIResponse saveBets(SaveBetRequestDto saveBetRequestDto, String authHeader) {
        if (saveBetRequestDto.getId() < 0 || saveBetRequestDto.getId() > 5) {
            return APIResponse.get(StatusCode.FAILED);
        }

        Long playerId = getPlayerId(authHeader);

        List<SaveDetails> saveDetails =
                saveDetailsRepository.findByUserIdAndTableId(
                        playerId,
                        saveBetRequestDto.getTableId());

        SaveDetails targetSaveBet = saveDetails.stream()
                .filter(a -> Objects.equals(a.getOrder(), saveBetRequestDto.getId()))
                .findFirst()
                .orElse(new SaveDetails());

        boolean isNew = targetSaveBet.getId() == null;

        Map<String, Object> betDetails = new LinkedHashMap<>();
        betDetails.put("mainBets", saveBetRequestDto.getMainBets());
        betDetails.put("sideBets", saveBetRequestDto.getSideBets());
        betDetails.put("jackPotNumbers", saveBetRequestDto.getJackPotNumbers());

        targetSaveBet.setValue(betDetails);
        targetSaveBet.setUserId(playerId);
        targetSaveBet.setTableId(saveBetRequestDto.getTableId());
        targetSaveBet.setOrder(saveBetRequestDto.getId());

        SaveDetails saved = saveDetailsRepository.save(targetSaveBet);

        if (isNew) {
            saveDetails.add(saved);
        } else {
            saveDetails.replaceAll(a ->
                    Objects.equals(a.getOrder(), saved.getOrder())
                            ? saved
                            : a);
        }

        List<SaveBetResponseDto> collect = saveDetails.stream()
                .map(a -> new SaveBetResponseDto(
                        a.getValue(),
                        a.getOrder()))
                .collect(Collectors.toList());

        return APIResponse.success(collect);

    }

    public APIResponse getSavedBets(String authHeader, String tableId) {
        Long playerId = getPlayerId(authHeader);
        List<SaveDetails> saveDetails = saveDetailsRepository.findByUserIdAndTableId(playerId, tableId);
        List<SaveBetResponseDto> collect = new LinkedList<>();
        if (!saveDetails.isEmpty())
            collect = saveDetails.stream().map(a -> new SaveBetResponseDto(a.getValue(), a.getOrder())).collect(Collectors.toList());
        return APIResponse.success(collect);
    }

    public APIResponse deleteSavedBets(String authHeader, SaveBetRequestDto saveBetRequestDto) {
        Long playerId = getPlayerId(authHeader);
        saveDetailsRepository.deleteByUserIdAndTableIdAndOrder(playerId, saveBetRequestDto.getTableId(),saveBetRequestDto.getId());
        return APIResponse.success("deleted");
    }

    private Long getPlayerId(String authHeader) {
        String token = authHeader.substring(7);
        DecodedJWT jwt = JwtTokenUtil.getDecodedJWT(token);
        Long playerId = Long.valueOf(jwt.getClaim("playerId").asString());
        return playerId;
    }

}
