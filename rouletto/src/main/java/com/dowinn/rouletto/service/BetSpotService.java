package com.dowinn.rouletto.service;

import com.dowinn.rouletto.dto.gateway.response.BetLimitResponse;
import com.dowinn.rouletto.dto.socket.response.BetSpotData;
import com.dowinn.rouletto.entity.GameBetSpot;
import com.dowinn.rouletto.entity.JackpotCasinoMap;
import com.dowinn.rouletto.entity.PayOff;
import com.dowinn.rouletto.entity.Spots;
import com.dowinn.rouletto.redis.RedisHelper;
import com.dowinn.rouletto.repository.GameBetSpotRepository;
import com.dowinn.rouletto.repository.JackpotCasinoMapRepository;
import com.dowinn.rouletto.socket.SessionData;
import com.dowinn.rouletto.util.CurrencyUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public  class BetSpotService  {

    @Autowired
    GameBetSpotRepository gameBetSpotRepository;

    @Autowired
    GatewayService gatewayService;

    @Autowired
    JackPotService jackPotService;

    @Autowired
    RedisHelper redisHelper;

    @Autowired
    @Lazy
    CurrencyUtil currencyUtil;


    public static Map<Integer, Spots> spotIndexMap = new LinkedHashMap<>();

    public static Map<String, Spots> spotValueMap = new LinkedHashMap<>();

    public static Map<Long, GameBetSpot> operatorSpotMap = new LinkedHashMap<>();


    public Map<Integer,String> getGameBetSpots(String casinoId, Integer betLimitTypeId, String currency, String tableId, Integer rtp) {

        Map<Integer, String> gameBetSpot = redisHelper.getGameBetSpot(casinoId, tableId, currency, betLimitTypeId);
        if(gameBetSpot!=null){
            log.info("betspot is presenet");
            return gameBetSpot;
        }

        List<GameBetSpot> gameBetSpots = gameBetSpotRepository.findBytableIdAndCasinoIdAndCurrencyAndBetLimitTypeIdAndSpot_Active(tableId, casinoId, currency, betLimitTypeId, true);
        Map<Integer, String> spotData = gameBetSpots
                .stream()
                .collect(Collectors.toMap(a -> a.getSpot().getIndex(), a -> collectSpotMap(a, rtp)));

        redisHelper.setGameBetSpot(casinoId, tableId, currency, betLimitTypeId,spotData);

        if (spotIndexMap.isEmpty()) {
            gameBetSpots.forEach(a -> spotIndexMap.put(a.getSpot().getIndex(), a.getSpot()));
        }
        if (spotValueMap.isEmpty()) {
            gameBetSpots.forEach(a -> spotValueMap.put(a.getSpot().getValue(), a.getSpot()));
        }
        gameBetSpots.forEach(a -> operatorSpotMap.put(a.getId(), a));
        return spotData;
    }

    private static String collectSpotMap(GameBetSpot a, Integer rtp) {
        PayOff payOff = a.getSpot().getPayOff().stream().filter(tem -> tem.getRtp() == rtp).findFirst().get();
        return payOff.getMultiPayOff() != null ? a.getSpot().getValue() +
                "|" + a.getMin() +
                "|" + a.getMax() +
                "|" + payOff.getMultiPayOff() :
                a.getSpot().getValue() +
                        "|" + a.getMin() +
                        "|" + a.getMax() +
                        "|" + payOff.getPayoff();
    }

    public boolean checkBetLimit(Map<Integer, Double> spots, String casinoId, String currency, Integer betLimitId, String tableId) {
        List<GameBetSpot> betSpots = getPlayerBetSpot(casinoId, betLimitId, currency, tableId);

        for (Map.Entry spot : spots.entrySet()) {
            Integer spotIndex = (Integer) spot.getKey();
            Double betAmount = (Double) spot.getValue();
            GameBetSpot gameBetSpot = betSpots.stream().filter(a -> Objects.equals(a.getSpot().getIndex(), spotIndex)).findFirst().get();
                if (betAmount < gameBetSpot.getMin() || betAmount > gameBetSpot.getMax()) {
                    log.info("spot index {} gameBetSpot.getMin() {} gameBetSpot.getMax() {} ",spotIndex,gameBetSpot.getMin(),gameBetSpot.getMax());
                    return false;
                }
        }
        return true;
    }

    public static Spots getBetSpot(String spotValue) {
        return spotValueMap.get(spotValue);
    }

    public static Spots getBetSpot(Integer spotIndex) {
        return spotIndexMap.get(spotIndex);
    }

    public BetSpotData getPlayerSpotLimits(SessionData sessionData) {
        log.info("Casino Id {}", sessionData.getCasinoId());
        Map<Integer, String> gameBetSpots = getGameBetSpots(sessionData.getCasinoId(), sessionData.getBetlimitId(), sessionData.getCurrency(), sessionData.getTableId(), sessionData.getRtp());
        JackpotCasinoMap jackPotDetails = jackPotService.getJackPotDetails(sessionData.getCasinoId());
        BetSpotData betSpotData = new BetSpotData();
        betSpotData.setTableId(sessionData.getTableId());
        betSpotData.setVariantId(List.of(1, 2, 3));//todo variantId settings
        betSpotData.setJackpotMaxTickets(jackPotDetails.getTicketCount());
        betSpotData.setJackpotTicketStake(currencyUtil.getPlayerAmount(sessionData.getPlayerId(), BigDecimal.valueOf(jackPotDetails.getTicketAmount()),sessionData.getCurrency()).doubleValue());
        betSpotData.setSpotLimit(gameBetSpots);
        betSpotData.setRtp(sessionData.getRtp());
        BetLimitResponse betLimitResponse=redisHelper.getPlayerBetLimit(sessionData.getCasinoId(),sessionData.getTableId(),sessionData.getCurrency(),sessionData.getBetlimitId());
        if(betLimitResponse!=null){
            betSpotData.setBetLimit(betLimitResponse);
        }else {
            ResponseEntity<BetLimitResponse> response = gatewayService.betLimitResponse(sessionData.getTableId(), sessionData.getBetlimitId(), sessionData.getCasinoId(), sessionData.getCurrency());
            if (response.getStatusCode().is2xxSuccessful()) {
                betLimitResponse = response.getBody();
                betSpotData.setBetLimit(betLimitResponse);
                redisHelper.setPlayerBetLimit(sessionData.getCasinoId(), sessionData.getTableId(),sessionData.getCurrency(),sessionData.getBetlimitId(),betLimitResponse);
            }
        }
        return betSpotData;
    }

    public static List<GameBetSpot> getPlayerBetSpot(String casinoId, Integer betlimitTypeId, String currency, String tableId) {
        return operatorSpotMap.values().stream().filter(a -> a.getCasinoId().equals(casinoId)
                && a.getBetLimitTypeId() == betlimitTypeId
                && a.getCurrency().equals(currency)
                && a.getTableId().equals(tableId)).collect(Collectors.toList());
    }


}
