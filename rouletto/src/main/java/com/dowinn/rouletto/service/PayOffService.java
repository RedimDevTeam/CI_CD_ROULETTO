package com.dowinn.rouletto.service;

import com.dowinn.rouletto.communication.PlayerCommunication;
import com.dowinn.rouletto.communication.StatusCode;
import com.dowinn.rouletto.dto.gateway.response.FundtransferResponse;
import com.dowinn.rouletto.dto.socket.response.BalanceData;
import com.dowinn.rouletto.dto.socket.response.PlayerResultData;
import com.dowinn.rouletto.entity.*;
import com.dowinn.rouletto.enums.GameStatus;
import com.dowinn.rouletto.enums.Timers;
import com.dowinn.rouletto.enums.TransactionSubType;
import com.dowinn.rouletto.model.GameDetail;
import com.dowinn.rouletto.model.GameResult;
import com.dowinn.rouletto.repository.BetRepository;
import com.dowinn.rouletto.repository.TransactionRepository;
import com.dowinn.rouletto.util.DateTimeUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PayOffService {

    @Autowired
    BetRepository betRepository;

    @Autowired
    JackPotService jackPotService;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    FundService fundService;

    @Autowired
    PlayerCommunication playerCommunication;



    public void initiatePayOff(GameDetail gameDetail) {

        List<Bets> bets = betRepository.findByGameId(gameDetail.getGameId());

        GameResult gameResult = gameDetail.getGameResult();
        log.info("bets {}", bets);
        log.info("game result {}", gameResult);
        for (Bets bet : bets) {
            calculateBetPayoff(bet, gameResult);
        }
        saveBets(bets);
        List<JackpotBet> jackpotBets = jackPotService.checkJackpot(gameDetail);
        gameDetail.setStatus(GameStatus.COMPLETED);
        settlePayOff(bets, jackpotBets, gameDetail);
        Integer diff = gameDetail.getTimers().get(Timers.DIFF_TIMER);
        try {
            Thread.sleep(diff*1000);
            sendResult(bets, jackpotBets, gameDetail);
        } catch (InterruptedException e) {
            log.info("e");
        }
    }

    public void sendResult(List<Bets> bets, List<JackpotBet> jackpotBets, GameDetail gameDetail) {
        Map<Long, List<Bets>> playersBets = bets.stream().collect(Collectors.groupingBy(Bets::getPlayerId));
        Map<Long, List<JackpotBet>> playerJackPotBet = jackpotBets.stream().collect(Collectors.groupingBy(JackpotBet::getPlayerId));
        Set<Long> players = new LinkedHashSet<>();
        players.addAll(playersBets.keySet());
        players.addAll(playerJackPotBet.keySet());

        List<Integer> winningSpot = gameDetail.getGameResult().getWinningSpot();
        //winning spots
        Map<Integer, List<Integer>> ws = winningSpot.stream().collect(Collectors.groupingBy(a -> BetSpotService.spotIndexMap.get(a).getVariant()));
        Integer secs = DateTimeUtil.remainingSeconds(gameDetail.getEnd(), gameDetail.getTimers().get(Timers.RESULT_TIMER));
        Integer diff = DateTimeUtil.completedSeconds(gameDetail.getEnd(), gameDetail.getTimers().get(Timers.RESULT_TIMER));
        Integer baseSec = gameDetail.getTimers().get(Timers.RESULT_TIMER);
        //publish result for betted players
        for (Long player : players) {
            PlayerResultData response = new PlayerResultData();

            response.setGameId(gameDetail.getGameId());
            response.setBall(gameDetail.getBalls());
            response.setBaseSecs(baseSec);
            response.setSecs(secs);
            response.setDiff(diff);

            PlayerResultData.PlayerData playerData = new PlayerResultData.PlayerData();
            //set winning spots
            playerData.setWs(ws);

            List<JackpotBet> playerJackpot = playerJackPotBet.getOrDefault(player, Collections.emptyList());
            Double jackPotBetAmount = 0.0;
            Double jackPotWinAmount = 0.0;

            boolean progressiveJackpot = playerJackpot.stream().anyMatch(a -> {
                return a.getBetNumbers().containsAll(gameDetail.getBalls());
            });
            if (!playerJackpot.isEmpty()) {
                List<PlayerResultData.JackPotResult> playerJackpotResult = playerJackpot.stream().map(a -> {
                    PlayerResultData.JackPotResult jackPotResult = new PlayerResultData.JackPotResult();
                    jackPotResult.setId(a.getId());
                    jackPotResult.setBalls(a.getBetNumbers());
                    jackPotResult.setStake(a.getAmount());
                    jackPotResult.setPayoff(a.getPayoff());
                    return jackPotResult;
                }).collect(Collectors.toList());
                Map<Integer, List<PlayerResultData.JackPotResult>> jp = new LinkedHashMap<>();
                jp.put(3, playerJackpotResult);
                playerData.setJackpotResult(jp);
                jackPotBetAmount = playerJackpot.stream().mapToDouble(JackpotBet::getAmount).reduce(0, (a, b) -> a + b);

                jackPotWinAmount = playerJackpot.stream().filter(a -> a.getPayoff() > 0).mapToDouble(JackpotBet::getPayoff).sum();

            } else {
                playerData.setJackpotResult(null);
            }
            //set player side bets
            double totalBetAmount = 0.0, totalWinAmount = 0.0;
            List<Bets> playerbets = playersBets.getOrDefault(player, Collections.emptyList());
            if (!playerbets.isEmpty()) {
                totalBetAmount = playerbets.stream().mapToDouble(Bets::getAmount).reduce(0, (a, b) -> a + b);
                totalWinAmount = playerbets.stream().filter(a->a.getRealPayoff()>0).mapToDouble(Bets::getRealPayoff).sum();
                // set winning payoff
                Map<Integer, Map<String, Double>> wp = playerbets.stream().
                        collect(Collectors.groupingBy(a -> a.getVariantId(),
                                        Collectors.toMap(
                                                a -> String.valueOf(a.getSpotIndex()), a -> a.getRealPayoff()
                                        )
                                )
                        );
                playerData.setWp(wp);
            } else {
                playerData.setWp(null);
            }
            playerData.setWin(gameDetail.getBalls());
            response.setTotalBetAmount(totalBetAmount + jackPotBetAmount);
            response.setTotalWinAmount(totalWinAmount + jackPotWinAmount);
            response.setJackPotWinAmount(jackPotWinAmount);
            response.setProgressiveJackPot(progressiveJackpot);
            response.setResults(playerData);
            playerCommunication.sendPlayerMessage(response, StatusCode.RESULT_TIMER, player);
        }
        //publish result for non betted player
        PlayerResultData response = new PlayerResultData();
        response.setGameId(gameDetail.getGameId());
        response.setBall(gameDetail.getBalls());
        response.setBaseSecs(baseSec);
        response.setSecs(secs);
        response.setDiff(diff);
        PlayerResultData.PlayerData playerData = new PlayerResultData.PlayerData();
        playerData.setWs(ws);
        response.setResults(playerData);
        playerCommunication.sendTableMessageExcludePlayer(response, StatusCode.RESULT_TIMER, gameDetail.getTableId(), players);
    }


    @Async
    private void settlePayOff(List<Bets> bets, List<JackpotBet> jackpotBets, GameDetail gameDetail) {
        Map<Long, List<Bets>> playersBets = bets.stream().collect(Collectors.groupingBy(Bets::getPlayerId));
        Map<Long, List<JackpotBet>> playerJackPotBet = jackpotBets.stream().collect(Collectors.groupingBy(JackpotBet::getPlayerId));
        Set<Long> players = new LinkedHashSet<>();
        players.addAll(playersBets.keySet());
        players.addAll(playerJackPotBet.keySet());

        for (Long player :players ) {

            List<JackpotBet> playerJackpot = playerJackPotBet.getOrDefault(player, Collections.emptyList());
            Double jackPotBetAmount = 0.0, jackPotWinAmount = 0.0;
            if (!playerJackpot.isEmpty()) {
                jackPotBetAmount = playerJackpot.stream().mapToDouble(JackpotBet::getAmount).reduce(0, (a, b) -> a + b);
                jackPotWinAmount = playerJackpot.stream().filter(a -> a.getPayoff() > 0).mapToDouble(JackpotBet::getPayoff).sum();
            }

            List<Bets> playerbets = playersBets.getOrDefault(player, Collections.emptyList());
            double totalBetAmount = 0.0, totalWinAmount = 0.0;
            if (!playerbets.isEmpty()) {
                totalBetAmount = playerbets.stream().mapToDouble(Bets::getAmount).reduce(0, (a, b) -> a + b);
                totalWinAmount = playerbets.stream().filter(a->a.getRealPayoff()>0).mapToDouble(Bets::getRealPayoff).sum();
            }

            log.info("playerId :: {} totalBetAmount :: {} jackpotBetAmount :: {} totalWinAmount :: {} jackPotWinAmount :: {}", player, totalBetAmount, jackPotBetAmount, totalWinAmount, jackPotWinAmount);

            String currency=!playerbets.isEmpty()?playerbets.get(0).getCurrency():playerJackpot.get(0).getCurrency();

            Transaction transaction = fundService.createTransaction(player, totalWinAmount + jackPotWinAmount, gameDetail, currency, TransactionSubType.WIN);

            FundtransferResponse fundTransferResponse = fundService.handlePartnerTransaction(totalWinAmount + jackPotWinAmount, player, gameDetail, currency, TransactionSubType.WIN, transaction);

            fundService.updateTransaction(transaction, playerbets, playerJackpot);

            if (fundTransferResponse != null && "0".equals(fundTransferResponse.getStatus())) {
                sendBalance(player, fundTransferResponse.getAmount());
            } else {
                sendBalance(player, BigDecimal.ZERO);
            }
        }
    }

    private void calculateBetPayoff(Bets bet, GameResult gameResult) {
        //  Spots spots = gameResult.getSpots().stream().filter(a -> a.getIndex() == bet.getSpotIndex()).findAny().orElse(null);

        Integer spotIndex = gameResult.getWinningSpot().stream().filter(a -> Objects.equals(a, bet.getSpotIndex())).findAny().orElse(null);
        Spots spots = BetSpotService.getBetSpot(spotIndex);
        double payoff = 0;
        log.info("spots {}", spots);
        if (spots == null) {
            bet.setRealPayoff(bet.getAmount() * -1);
        } else {
            PayOff payOff = spots.getPayOff().stream().filter(a -> Objects.equals(a.getRtp(), bet.getRtp())).findAny().orElse(null);
            if (payOff.getMultiPayOff() == null) {
                payoff = payOff.getPayoff();
            } else {
                payoff = getPayOff(spots, gameResult, payOff);
            }
            double winAmount = bet.getAmount() * (payoff);

            bet.setRealPayoff(winAmount);
        }
        log.info("payOff :: {} , bet amount :: {} , spot index :: {} rtp :: {} , payOff :: {}", payoff, bet.getAmount(), bet.getSpotIndex(), bet.getRtp(), bet.getRealPayoff());

    }

    private void sendBalance(Long player, BigDecimal amount) {
        BalanceData balanceData = new BalanceData(amount);
        playerCommunication.sendPlayerMessage(balanceData, StatusCode.UPDATE_BALANCE, player);
    }

    private double getPayOff(Spots spot, GameResult gameResult, PayOff payOff) {
        Integer ballCount = gameResult.getSpotBallCount().get(spot.getIndex());
        log.info("payOff {}", payOff);
        String[] val = payOff.getMultiPayOff().split("\\|");
        log.info("val {}", val);
        for (String spotData : val) {
            String[] spotValue = spotData.split("-");
            log.info("spotValue {}", spotData);
            int balls = Integer.parseInt(spotValue[0]);
            if (ballCount == balls) {
                return Double.parseDouble(spotValue[1]);
            }
        }
        return 0;
    }


    public void settleCancelledGame(GameDetail gameDetail) {
        List<Bets> mainBets = betRepository.findByGameId(gameDetail.getGameId());
        List<JackpotBet> gameJackpotBets = jackPotService.getGameJackpotBet(gameDetail.getGameId());
        //reset amount for cancel
        mainBets.forEach(a -> a.setRealPayoff(a.getAmount()));
        gameJackpotBets.forEach(a -> a.setPayoff(a.getAmount()));

        //save bets for refund
        saveBets(mainBets);
        jackPotService.updatePlayerJackPotBet(gameJackpotBets);

        Map<Long, List<Bets>> playersBets = mainBets.stream().collect(Collectors.groupingBy(Bets::getPlayerId));
        Map<Long, List<JackpotBet>> playerJackPotBet = gameJackpotBets.stream().collect(Collectors.groupingBy(JackpotBet::getPlayerId));
        Set<Long> players = new LinkedHashSet<>();
        players.addAll(playersBets.keySet());
        players.addAll(playerJackPotBet.keySet());


        for (Long player : players) {

            List<Bets> playerbets = playersBets.getOrDefault(player, Collections.emptyList());
            double totalBetAmount = 0.0, totalWinAmount = 0.0;
            if (!playerbets.isEmpty()) {
                totalBetAmount = playerbets.stream().mapToDouble(Bets::getAmount).reduce(0, (a, b) -> a + b);
                totalWinAmount = playerbets.stream().filter(a->a.getRealPayoff()>0).mapToDouble(Bets::getRealPayoff).sum();
            }

            List<JackpotBet> playerJackpot = playerJackPotBet.getOrDefault(player, Collections.emptyList());
            Double jackPotBetAmount = 0.0, jackPotWinAmount = 0.0;
            if (!playerJackpot.isEmpty()) {
                jackPotBetAmount = playerJackpot.stream().mapToDouble(JackpotBet::getAmount).reduce(0, (a, b) -> a + b);
                jackPotWinAmount = playerJackpot.stream().filter(a -> a.getPayoff() > 0).mapToDouble(JackpotBet::getPayoff).sum();
            }


            log.info("playerId :: {} totalBetAmount :: {} jackpotBetAmount :: {} totalWinAmount :: {} jackPotWinAmount :: {}", player, totalBetAmount, jackPotBetAmount, totalWinAmount, jackPotWinAmount);

            String currency=!playerbets.isEmpty()?playerbets.get(0).getCurrency():playerJackpot.get(0).getCurrency();

            Transaction transaction = fundService.createTransaction(player, totalWinAmount + jackPotWinAmount, gameDetail,currency, TransactionSubType.CANCEL);

            FundtransferResponse fundTransferResponse = fundService.handlePartnerTransaction(totalWinAmount + jackPotWinAmount, player, gameDetail, currency, TransactionSubType.CANCEL, transaction);

            fundService.updateTransaction(transaction, playerbets, playerJackpot);

            if (fundTransferResponse != null && "0".equals(fundTransferResponse.getStatus())) {
                sendBalance(player, fundTransferResponse.getAmount());
            } else {
                sendBalance(player, BigDecimal.ZERO);
            }
        }

    }

    @Transactional
    private void saveBets(List<Bets> bets) {
        betRepository.saveAll(bets);
    }

}
