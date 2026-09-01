package com.dowinn.rouletto.redis;


import com.dowinn.rouletto.dto.gateway.response.BetLimitResponse;
import com.dowinn.rouletto.dto.socket.response.PastResultDto;
import com.dowinn.rouletto.entity.GameBetSpot;
import com.dowinn.rouletto.entity.TableConfig;
import com.dowinn.rouletto.util.FunctionUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.helper.FunctionHelper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RedisHelper {


    @Value("${redis.host}")
    String redisIp;

    @Value("${redis.port}")
    Integer redisPort;

    @Autowired
    ObjectMapper objectMapper;

   public  void setGameBetSpot(String casinoId,String tableId,String currency,Integer betlimit,Map<Integer,String> gameBetSpots){
       try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
           jedis.jsonSet(GameHelperRedis.GAME_BET_SPOT.getName() +casinoId+"_"+tableId+"_"+currency+"_"+betlimit , Path.of("$"), gameBetSpots);
       }
   }


    public Map<Integer, String> getGameBetSpot(String casinoId, String tableId, String currency, Integer betlimit){
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
            Object obj = jedis.jsonGet(GameHelperRedis.GAME_BET_SPOT.getName() + casinoId + "_" + tableId + "_" + currency + "_" + betlimit);
            if (obj != null) {
                try {
                    String serialize = FunctionUtil.Serialize(obj);
                    Map<Integer, String> gameBetSpots = objectMapper.readValue(serialize, new TypeReference<Map<Integer,String>>() {});
                    return gameBetSpots;
                } catch (Exception e) {
                    log.info("exception {}",e);
                }
            }
        }
       return null;
    }

    public  void setPlayerBetLimit(String casinoId,String tableId,String currency,Integer betlimit,BetLimitResponse betLimitResponse){
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
            jedis.jsonSet(GameHelperRedis.BET_LIMIT.getName() +casinoId+"_"+tableId+"_"+currency+"_"+betlimit , Path.of("$"), betLimitResponse);
        }
    }


    public BetLimitResponse getPlayerBetLimit(String casinoId, String tableId, String currency, Integer betlimit){
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
            Object obj = jedis.jsonGet(GameHelperRedis.BET_LIMIT.getName() + casinoId + "_" + tableId + "_" + currency + "_" + betlimit);
            if (obj != null) {
                try {
                    String serialize = FunctionUtil.Serialize(obj);
                    com.dowinn.rouletto.dto.gateway.response.BetLimitResponse betLimitResponse = objectMapper.readValue(serialize, BetLimitResponse.class);
                    return betLimitResponse;
                } catch (Exception e) {
                    log.info("exception {}",e);
                }
            }
        }
        return null;
    }


    public  void setTableConfig(String tableId, TableConfig tableConfig){
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
            jedis.jsonSet(GameHelperRedis.TABLE_CONFIG.getName()+tableId, Path.of("$"), tableConfig);
        }
    }

    public TableConfig getTableConfig(String tableId){
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
            Object obj = jedis.jsonGet(GameHelperRedis.TABLE_CONFIG.getName()+tableId);
            if (obj != null) {
                try {
                    String serialize = FunctionUtil.Serialize(obj);
                    TableConfig tableConfig=FunctionUtil.Deserialze(serialize, TableConfig.class);
                    return tableConfig;
                } catch (Exception e) {
                    log.info("exception {}",e);
                }
            }
        }
        return null;
    }

    public  void setPastResult(String tableId,PastResultDto pastResultDto){
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
            jedis.jsonSet(GameHelperRedis.PAST_RESULT.getName()+tableId, Path.of("$"), pastResultDto);
        }
    }


    public PastResultDto getPastResult(String tableId){
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
            Object obj = jedis.jsonGet(GameHelperRedis.PAST_RESULT.getName()+tableId);
            if (obj != null) {
                try {
                    String serialize = FunctionUtil.Serialize(obj);
                    PastResultDto pastResultDto=FunctionUtil.Deserialze(serialize, PastResultDto.class);
                    return pastResultDto;
                } catch (Exception e) {
                    log.info("exception {}",e);
                }
            }
        }
        return null;
    }

    public void removeSession(String sessionId) {
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
            jedis.del("playersession:" + sessionId);
        }
    }

    public boolean isSessionPresent(String sessionId) {
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
            return jedis.exists("playersession:" + sessionId);
        }
    }

    @PostConstruct
    public void cleanupRedisKeys() {

        String cursor = ScanParams.SCAN_POINTER_START;

        ScanParams scanParams = new ScanParams()
                .match("gamebetspots*")
                .count(1000);
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {

            do {
                ScanResult<String> scanResult =
                        jedis.scan(cursor, scanParams);

                if (!scanResult.getResult().isEmpty()) {

                    jedis.del(
                            scanResult.getResult()
                                    .toArray(new String[0])
                    );

                    log.info(
                            "Deleted keys count : "
                                    + scanResult.getResult().size()
                    );
                }

                cursor = scanResult.getCursor();

            } while (!cursor.equals("0"));
        }
        System.out.println("Redis cleanup completed");
    }

    public  void setCurrencyUsdExchange(String currency, BigDecimal amount){
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
            jedis.set("currency:"+currency, String.valueOf(amount));
        }
    }

    public Boolean checkCurrencyExists(String currency){
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
            return jedis.exists("currency:"+currency);
        }
    }

    public BigDecimal getCurrencyRate(String currency){
        try (UnifiedJedis jedis = new UnifiedJedis("redis://" + redisIp + ":" + redisPort)) {
            String obj = jedis.get(GameHelperRedis.CURRENCY_RATE.getName()+currency);
            if (obj != null) {
                return new BigDecimal(obj);
            }

            return null;
        }
    }



}
