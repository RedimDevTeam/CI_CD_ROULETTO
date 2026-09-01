package com.dowinn.rouletto.service;


import com.dowinn.rouletto.entity.Jackpot;
import com.dowinn.rouletto.entity.JackpotBet;
import com.dowinn.rouletto.entity.JackpotCasinoMap;
import com.dowinn.rouletto.model.GameDetail;
import com.dowinn.rouletto.model.JackpotBetMap;
import com.dowinn.rouletto.repository.JackpotBetRepository;
import com.dowinn.rouletto.repository.JackpotCasinoMapRepository;
import com.dowinn.rouletto.repository.JackpotRepository;
import com.dowinn.rouletto.socket.SessionData;
import com.dowinn.rouletto.util.CurrencyUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JackPotService {

    @Autowired
    JackpotBetRepository jackpotBetRepository;

    @Autowired
    JackpotRepository jackpotRepository;

    @Autowired
    JackpotCasinoMapRepository jackpotCasinoMapRepository;

    @Autowired
    @Lazy
    PlayerActionService playerActionService;

    @Autowired
    CurrencyUtil currencyUtil;


    private static List<JackpotCasinoMap> jackpotCasinoMaps = new LinkedList<>();


    public List<JackpotBet> checkJackpot(GameDetail gameDetail) {

        List<JackpotBet> playersJackPotBet = jackpotBetRepository.findAllByGameId(gameDetail.getGameId());
        checkProgressiveJackpot(playersJackPotBet, gameDetail);
        for (JackpotBet playerBet : playersJackPotBet) {
            validateJackpot(playerBet, gameDetail.getBalls());
        }
        jackpotBetRepository.saveAll(playersJackPotBet);
        return playersJackPotBet;

    }

    private void checkProgressiveJackpot(List<JackpotBet> playersJackPotBet, GameDetail gameDetail) {
        Map<String, Map<Long, List<JackpotBet>>> jackpotWinners = playersJackPotBet.stream()
                .filter(a -> a.getBetNumbers().containsAll(gameDetail.getBalls()))
                .collect(Collectors.groupingBy(
                        JackpotBet::getCasino, Collectors.groupingBy(
                                JackpotBet::getPlayerId))
                );
        log.info("jackpot winners based on casino {}", jackpotWinners);
        if (jackpotWinners != null) {
            for (String casino : jackpotWinners.keySet()) {
                // Double accumalationRate = getAccumalationRate(casino);
                Jackpot jackpot = jackpotRepository.findByCasinoIdAndTableIdAndActiveTrue(casino, gameDetail.getTableId());
                if (jackpot != null) {
                    Map<Long, List<JackpotBet>> playerJackPot = jackpotWinners.get(casino);
                    for (Long player : playerJackPot.keySet()) {
                        List<JackpotBet> jackpotBets = playerJackPot.get(player);

                        double cumulativeAmount = jackpot.getJackpotAmount() / playerJackPot.values().stream().flatMap(a->a.stream()).count();
                        log.info("payOff {}", cumulativeAmount);
                        for (JackpotBet jackpotBet : jackpotBets) {
                          
                            BigDecimal playerCurrencyPayOff=currencyUtil.getPlayerAmount(player, BigDecimal.valueOf(cumulativeAmount),jackpotBet.getCurrency());
                            jackpotBet.setPayoff(playerCurrencyPayOff.doubleValue());
                        }
                    }
                }
            }

            List<Jackpot> casinoJackpot = jackpotRepository.findByCasinoIdInAndTableIdAndActiveTrue(new ArrayList<>(jackpotWinners.keySet()), gameDetail.getTableId());
            for (Jackpot jackpot : casinoJackpot) {
                jackpot.setGameId(gameDetail.getGameId());
                log.info("player ids {}", jackpotWinners.get(jackpot.getCasinoId()).keySet());
                jackpot.setPlayerId(new ArrayList<>(jackpotWinners.get(jackpot.getCasinoId()).keySet()));
                jackpot.setActive(false);
            }
            jackpotRepository.saveAll(casinoJackpot);
            casinoJackpot.forEach(a -> playerActionService.publishJackPotMessage(BigDecimal.valueOf(getJackPotDetails(a.getCasinoId()).getJackpotConfig().getAmount()), a.getCasinoId()));
            jackpotWinners.keySet().forEach(casino -> createJackPot(gameDetail.getTableId(), casino));
        }

    }

    @Transactional
    public JackpotBetMap savePlayerJackpot(SessionData sessionData, List<List<Integer>> playerTicket, GameDetail gameDetail) {
       try {
           log.info("savePlayerJackpot START gameId={}, playerId={}, thread={}", gameDetail.getGameId(), sessionData.getPlayerId(), Thread.currentThread().getName());
           Jackpot jackpot = jackpotRepository.findByCasinoIdAndTableIdAndActiveTrue(sessionData.getCasinoId(), sessionData.getTableId());
           log.info("Jackpotid {}",jackpot.getJid());
           log.info("player name {} player id {} player casino {}", sessionData.getUserName(), sessionData.getPlayerId(), sessionData.getCasinoId());
           Double accumalationRate = getAccumalationRate(jackpot.getCasinoId());
           Double ticketAmount = getTicketAmount(jackpot.getCasinoId());
           Double playerJackPotBetAmount = playerTicket.size() * ticketAmount;

           Double jackpotBetAmount = (playerJackPotBetAmount) * (accumalationRate / 100);
           log.info("accumalation rate {} ticket amount {} jack pot bet amount {}", accumalationRate, ticketAmount, jackpotBetAmount);
           double currentAmount = jackpot.getJackpotAmount() != null ? jackpot.getJackpotAmount() : 0;
           double jackpotCeiling = jackpotCeiling(jackpot.getCasinoId());
           log.info("Before Update: {} ceiling: {}", currentAmount, jackpotCeiling);
           if (currentAmount >= jackpotCeiling) {
               log.info("Jackpot already at ceiling {}, skipping pool increment of {}", jackpotCeiling, jackpotBetAmount);
               jackpotBetAmount = 0.0;
           } else if (currentAmount + jackpotBetAmount > jackpotCeiling) {
               double Contribution = jackpotCeiling - currentAmount;
               log.info("Jackpot contribution  from {} to {} (ceiling={})", jackpotBetAmount, Contribution, jackpotCeiling);
               jackpotBetAmount = Contribution;
           }
           Long jackpotId = jackpot.getJid();
           if (jackpotBetAmount > 0) {
               int rows = jackpotRepository.updateJackpotAmount(jackpotId, jackpotBetAmount);
               log.info("Updated rows {}", rows);
           }
          
           Jackpot updatedJackpot = jackpotRepository.findById(jackpotId).orElseThrow();
           log.info("DB Jackpot Amount After Update {}", updatedJackpot.getJackpotAmount());
           List<JackpotBet> jackpotBets = new ArrayList<>();
           for (List<Integer> ticket : playerTicket) {
               JackpotBet jackpotBet = new JackpotBet();
               jackpotBet.setPlayerId(sessionData.getPlayerId());
               jackpotBet.setJackpotId(jackpot.getJid());
               jackpotBet.setCasino(sessionData.getCasinoId());
               jackpotBet.setGameId(gameDetail.getGameId());
               jackpotBet.setTableId(sessionData.getTableId());
               jackpotBet.setAmount(currencyUtil.getPlayerAmount(sessionData.getPlayerId(), BigDecimal.valueOf(ticketAmount), sessionData.getCurrency()).doubleValue());
               jackpotBet.setCurrency(sessionData.getCurrency());
               jackpotBet.setBetNumbers(ticket);
               jackpotBet.setCreatedAt(LocalDate.now());
               jackpotBets.add(jackpotBet);
           }
           List<JackpotBet> playerJackPot = jackpotBetRepository.saveAll(jackpotBets);
           return new JackpotBetMap(updatedJackpot, playerJackPot);
       }catch (Exception e){
           log.info("check jackpot map exception {}",e);
       }
       return null;
    }


    public Jackpot createJackPot(String tableId, String casinoId) {
        try {
            JackpotCasinoMap jackPotDetails = getJackPotDetails(casinoId);
            Jackpot jackpot = new Jackpot();
            jackpot.setActive(true);
            jackpot.setTableId(tableId);
            jackpot.setCasinoId(casinoId);
            jackpot.setCreatedAt(LocalDate.now());
            double initialAmount = jackPotDetails.getJackpotConfig().getAmount();
            Long ceiling = jackPotDetails.getJackpotCeiling();
            if (ceiling != null) {
                initialAmount = Math.min(initialAmount, ceiling.doubleValue());
            }
            jackpot.setJackpotAmount(initialAmount);
            return jackpotRepository.save(jackpot);
        } catch (Exception e) {
            log.info("exception e {}", e);
            return playerCasinoJackpot(tableId, casinoId);
        }
    }

    private void validateJackpot(JackpotBet playerBet, List<Integer> balls) {
        long count = balls.stream().filter(a -> playerBet.getBetNumbers().contains(a)).count();
        double payoff = 0;
        if (count == 4) {
            return;
        } else if (count == 3) {
            payoff = playerBet.getAmount() * 101.0;
        } else if (count == 2) {
            payoff = playerBet.getAmount() * 6.0;
        } else if (count == 1) {
            payoff = playerBet.getAmount() * 1.0;
        } else {
            payoff = playerBet.getAmount() * -1.0;
        }
        playerBet.setPayoff(payoff);
    }

    public void initJackpot() {
        List<JackpotCasinoMap> jackpot = jackpotCasinoMapRepository.findAll();
        jackpotCasinoMaps.addAll(jackpot);
    }

    public Double getAccumalationRate(String casino) {
        JackpotCasinoMap jackpotCasinoMap = jackpotCasinoMaps.stream().filter(a -> a.getCasinoId().equals(casino)).findFirst().get();
        log.info("jackpotCasinoMap percentage {}", jackpotCasinoMap.getJackpotConfig().getPercentage());
        return jackpotCasinoMap.getJackpotConfig().getPercentage();
    }

    public Double getTicketAmount(String casino) {
        JackpotCasinoMap jackpotCasinoMap = jackpotCasinoMaps.stream().filter(a -> a.getCasinoId().equals(casino)).findFirst().get();
        log.info("jackpotCasinoMap percentage {}", jackpotCasinoMap.getTicketAmount());
        return jackpotCasinoMap.getTicketAmount();
    }

    public JackpotCasinoMap getJackPotDetails(String casino) {
        JackpotCasinoMap jackpotCasinoMap = jackpotCasinoMaps.stream().filter(a -> a.getCasinoId().equals(casino)).findFirst().get();
        return jackpotCasinoMap;
    }

    private double jackpotCeiling(String casinoId) {
        Long ceiling = getJackPotDetails(casinoId).getJackpotCeiling();
        return ceiling != null ? ceiling.doubleValue() : Double.MAX_VALUE;
    }

    public List<JackpotBet> getPlayerJackpotBet(String gameId, Long playerId) {
        List<JackpotBet> jackpotbets = jackpotBetRepository.findByGameIdAndPlayerId(gameId, playerId);
        return jackpotbets;
    }

    public List<JackpotBet> getGameJackpotBet(String gameId) {
        List<JackpotBet> jackpotbets = jackpotBetRepository.findAllByGameId(gameId);
        return jackpotbets;
    }

    public Jackpot playerCasinoJackpot(String tableId, String casinoId) {
        return jackpotRepository.findByCasinoIdAndTableIdAndActiveTrue(casinoId, tableId);
    }

    public void updatePlayerJackPotBet(List<JackpotBet> jackpotBets) {
        jackpotBetRepository.saveAll(jackpotBets);
    }

    public void updateCasinoJackPot(String casino) {
        JackpotCasinoMap jackpotCasinoMap = jackpotCasinoMapRepository.findByCasinoId(casino);
        if (jackpotCasinoMap != null) {
            jackpotCasinoMaps.removeIf(a -> a.getCasinoId().equals(casino));
            jackpotCasinoMaps.add(jackpotCasinoMap);
        }
    }
}
