package com.dowinn.rouletto.service;


import com.dowinn.rouletto.communication.APIResponse;
import com.dowinn.rouletto.communication.PlayerCommunication;
import com.dowinn.rouletto.communication.StatusCode;
import com.dowinn.rouletto.dto.gateway.response.BalanceResponse;
import com.dowinn.rouletto.dto.gateway.response.FundtransferResponse;
import com.dowinn.rouletto.dto.socket.request.BetRequestDto;
import com.dowinn.rouletto.dto.socket.response.BalanceData;
import com.dowinn.rouletto.dto.socket.response.BetResultData;
import com.dowinn.rouletto.dto.socket.response.BetSpotData;
import com.dowinn.rouletto.dto.socket.response.PastResultDto;
import com.dowinn.rouletto.entity.*;
import com.dowinn.rouletto.enums.GameStatus;
import com.dowinn.rouletto.enums.Timers;
import com.dowinn.rouletto.enums.TransactionSubType;
import com.dowinn.rouletto.model.GameDetail;
import com.dowinn.rouletto.model.JackpotBetMap;
import com.dowinn.rouletto.repository.BetRepository;
import com.dowinn.rouletto.socket.SessionData;
import com.dowinn.rouletto.socket.SocketPool;
import com.dowinn.rouletto.util.CurrencyUtil;
import com.dowinn.rouletto.util.DateTimeUtil;
import com.dowinn.rouletto.util.FunctionUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlayerActionService {

    @Autowired
    BetSpotService betSpotService;

    @Autowired
    GameService gameService;

    @Autowired
    FundService fundService;

    @Autowired
    SessionManager sessionManager;

    @Autowired
    PlayerCommunication playerCommunication;

    @Autowired
    GatewayService gatewayService;

    @Autowired
    BetRepository betRepository;


    @Lazy
    @Autowired
    SocketPool socketPool;

    @Autowired
    PayOffService payOffService;

    @Autowired
    JackPotService jackPotService;

    @Autowired
    HistoryService historyService;

    @Autowired
    CurrencyUtil currencyUtil;

    public void handlePlayerInit(WebSocketSession session, SessionData sessionData) {
        playerCommunication.sendPlayerMessage("connected", StatusCode.CONNECTION_ESTABLISHED, sessionData.getPlayerId());

        sessionManager.updateSession(sessionData);
        BetSpotData betSpotData = betSpotService.getPlayerSpotLimits(sessionData);
        playerCommunication.sendPlayerMessage(betSpotData, StatusCode.SPOT_LIMITS, sessionData.getPlayerId());

        BalanceData balance = getBalance(session, sessionData);
        playerCommunication.sendPlayerMessage(balance, StatusCode.UPDATE_BALANCE, sessionData.getPlayerId());

        sendCurrentBet(sessionData, balance);
        sendCurrentJackPot(sessionData);

        gameService.sendBetTimer(sessionData.getTableId(), sessionData.getPlayerId());

        PastResultDto pastResultStats = historyService.getPastResultStats(sessionData.getTableId(),true);
        playerCommunication.sendPlayerMessage(pastResultStats, StatusCode.WS_ROULETTE_RESULT, sessionData.getPlayerId());
    }

    private void sendCurrentJackPot(SessionData sessionData) {
        Jackpot jackpot = jackPotService.playerCasinoJackpot(sessionData.getTableId(), sessionData.getCasinoId());
        Double jackpotAmount = jackpot != null ? jackpot.getJackpotAmount() : jackPotService.createJackPot(sessionData.getTableId(),sessionData.getCasinoId()).getJackpotAmount();
        sendBalance(sessionData.getPlayerId(), currencyUtil.getPlayerAmount(sessionData.getPlayerId(),BigDecimal.valueOf(jackpotAmount).setScale(3, RoundingMode.HALF_EVEN),sessionData.getCurrency()), StatusCode.UPDATE_JACKPOT);
    }

    private void sendCurrentBet(SessionData sessionData, BalanceData balanceData) {
        GameDetail gameDetail = gameService.getGameDetail(sessionData.getTableId());
        if (gameDetail != null) {
            GameStatus status = gameDetail.getStatus();
            log.info("status {}", status);
            List<Bets> playerBets = betRepository.findByGameIdAndPlayerId(gameDetail.getGameId(), sessionData.getPlayerId());
            List<JackpotBet> playerJackpotBet = jackPotService.getPlayerJackpotBet(gameDetail.getGameId(), sessionData.getPlayerId());
            switch (status) {
                case INPROGRESS -> {
                    BetResultData betResultData = new BetResultData();
                    Map<Integer, Double> bet = playerBets.stream().collect(Collectors.toMap(Bets::getSpotIndex, Bets::getAmount));
                    betResultData.setBetedSpots(bet);
                    betResultData.setBalance(balanceData.getBalance().doubleValue());
                    if (playerJackpotBet != null) {
                        List<List<Integer>> jackPotBet = playerJackpotBet.stream().map(a -> a.getBetNumbers()).collect(Collectors.toList());
                        betResultData.setJackPot(jackPotBet);
                    }else{betResultData.setJackPot(null);}
                    playerCommunication.sendPlayerMessage(betResultData, StatusCode.CONNECTION_CONFIRMED_BET, sessionData.getPlayerId());
                }
                case COMPLETED -> {
                    payOffService.sendResult(playerBets, playerJackpotBet, gameDetail);
                }
                default -> {
                }
            }
        }

    }

    public void handleConfirmBet(WebSocketSession session, String message, SessionData sessionData) {
        BetRequestDto betRequest = FunctionUtil.Deserialze(message, BetRequestDto.class);
        sessionManager.updateSession(sessionData);
        log.info("betRequest DTO {}", betRequest);

        Long playerId = sessionData.getPlayerId();
        String currency = sessionData.getCurrency();
        Integer betLimit = sessionData.getBetlimitId();
        String casinoId = sessionData.getCasinoId();
        String tableId = sessionData.getTableId();
        log.info("playerid :: {} currency :: {} betlimit :: {} casino :: {} table :: {} gameId {}", playerId, currency, betLimit, casinoId, tableId);
        socketPool.updateSession(playerId,sessionData);

        boolean isValid;

        GameDetail gameDetail = gameService.getGameDetail(tableId);
        log.info("game Details {}", gameDetail);
        if (gameDetail == null ) {
            playerCommunication.sendPlayerMessage("gamedetail null", StatusCode.BET_NOT_ACCEPTED, playerId);
            return;
        }
        if(gameDetail.getStatus()!=GameStatus.INPROGRESS){
            playerCommunication.sendPlayerMessage("game already completed or canceled", StatusCode.BET_NOT_ACCEPTED, playerId);
            return;
        }
        isValid = DateTimeUtil.checkValidTime(gameDetail.getStarts(), gameDetail.getTimers().get(Timers.BET_TIMER) + gameDetail.getTimers().get(Timers.DIFF_TIMER));

        if (!isValid) {
            playerCommunication.sendPlayerMessage("bet timer closed", StatusCode.BET_NOT_ACCEPTED, playerId);
            return;
        }
        isValid = betSpotService.checkBetLimit(betRequest.getBetSpots(), casinoId, currency, betLimit, tableId);
        if (!isValid) {
            playerCommunication.sendPlayerMessage("invalid bet limit", StatusCode.INVALID_BET_LIMIT, playerId);
            return;
        }

        Double betAmount = betRequest.getBetSpots().values().stream().reduce((double) 0, (a, b) -> a + b);
        Double totalBetAmount = 0.0;
        if (betRequest.getJackPot() != null) {
            Double ticketAmount = jackPotService.getTicketAmount(casinoId);
            Double jackpotBetAmount = betRequest.getJackPot().size() * ticketAmount;
            totalBetAmount = betAmount + jackpotBetAmount;
        } else {
            totalBetAmount = betAmount;
        }
        log.info("bet amount :: {} , playerid :: {} ,gameid :: {} ", totalBetAmount, playerId, gameDetail.getGameId());

        Transaction transaction = fundService.createTransaction(playerId, totalBetAmount, gameDetail, sessionData.getCurrency(), TransactionSubType.BET);
        FundtransferResponse fundTransferResponse = fundService.handlePartnerTransaction(totalBetAmount, playerId, gameDetail, currency, TransactionSubType.BET, transaction);
        if (fundTransferResponse != null && "0".equals(fundTransferResponse.getStatus())) {
            List<Bets> bets = saveBet(betRequest, sessionData, transaction);
            JackpotBetMap jackpot = null;
            if (betRequest.getJackPot() != null) {
                 jackpot = jackPotService.savePlayerJackpot(sessionData, betRequest.getJackPot(), gameDetail);
                 log.info("After savePlayerJackpot returned: {}", jackpot.getJackpot().getJackpotAmount());
                 publishJackPotMessage(BigDecimal.valueOf(jackpot.getJackpot().getJackpotAmount()), jackpot.getJackpot().getCasinoId());
            }

            if (jackpot != null) {
                fundService.updateTransaction(transaction, bets, jackpot.getJackpotBetList());
            } else {
                fundService.updateTransaction(transaction, bets, null);
            }
            sendBetConfirmed(betRequest, playerId, fundTransferResponse.getAmount());
        } else {
            sendBalance(playerId, BigDecimal.ZERO, StatusCode.BET_NOT_ACCEPTED);
        }
    }

    @Transactional
    private List<Bets> saveBet(BetRequestDto betRequest, SessionData sessionData, Transaction transaction) {
        List<Bets> playerBets = new LinkedList<>();


        for (Map.Entry bet : betRequest.getBetSpots().entrySet()) {
            Spots spots = BetSpotService.spotIndexMap.get((Integer) bet.getKey());
            Bets bets = new Bets();
            bets.setSpotIndex((Integer) bet.getKey());
            bets.setAmount((Double) bet.getValue());
            bets.setPlayerId(sessionData.getPlayerId());
            bets.setGameId(betRequest.getGameId());
            bets.setCasinoId(sessionData.getCasinoId());
            bets.setCurrency(sessionData.getCurrency());
            bets.setRtp(sessionData.getRtp());
            bets.setBetLimit(sessionData.getBetlimitId());
            bets.setVariantId(spots.getVariant());
            bets.setTableId(sessionData.getTableId());
            playerBets.add(bets);
        }
        List<Bets> bets = betRepository.saveAll(playerBets);
        return bets;
    }


    public BalanceData getBalance(WebSocketSession session, SessionData sessionData) {
        ResponseEntity<APIResponse> response = gatewayService.getBalance(sessionData.getIntegrationType(), sessionData.getPlayerId(), sessionData.getPlayerSessionId());
        if (response.getStatusCode().is2xxSuccessful()) {
            APIResponse apiResponse = response.getBody();
            log.info("response {} ", apiResponse);
            BalanceResponse balanceResponse = FunctionUtil.Deserialize(apiResponse.getResult(), BalanceResponse.class);
            BalanceData balanceData = new BalanceData();
            balanceData.setBalance(balanceResponse.getAmount());
            return balanceData;
        }
        return null;
    }

    public void sendBalance(long player, BigDecimal amount, StatusCode code) {
        BalanceData balanceData = new BalanceData();
        balanceData.setBalance(amount);
        playerCommunication.sendPlayerMessage(balanceData, code, player);
    }

    private void sendBetConfirmed(BetRequestDto betRequest, Long playerId, BigDecimal amount) {
        BetResultData betResultData = new BetResultData();
        betResultData.setBalance(amount.doubleValue());
        betResultData.setBetedSpots(betRequest.getBetSpots());
        betResultData.setJackPot(betRequest.getJackPot());
        playerCommunication.sendPlayerMessage(betResultData, StatusCode.BETS_CONFIRMED, playerId);
    }

    public void publishJackPotMessage(BigDecimal amount, String casinoId) {
        BalanceData balanceData = new BalanceData();
        balanceData.setBalance(amount.setScale(3, RoundingMode.HALF_EVEN));
        playerCommunication.sendPlayerJackpotMessages(balanceData, StatusCode.UPDATE_JACKPOT, casinoId);
    }

}
