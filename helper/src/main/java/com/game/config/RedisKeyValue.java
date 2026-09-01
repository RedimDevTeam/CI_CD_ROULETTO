/*
package com.game.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.helper.FunctionHelper;
import com.game.model.ActionTimerValue;
import com.game.model.CasinoCurrencyData;
import com.game.model.SocketStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class RedisKeyValue {

    public static final String table = "table_";
    public static final String casino = "casino_";
    public static final String casino_currency_value = "casino_currency_value_";
    public static final String all_currency_value = "all_currency_value_";
    public static final String all_video_record = "video_record_";
    public static final String typeIdByTableId = "typeId_";
    public static final String shortIdByTableId = "shortId_";
    public static final String configByTableId = "config_";
    public static final String singleDeckByTableId = "singleDeck_";
    public static final String gameIdByTableId = "gameId_";
    public static final String updateTimerByTableId = "updateTimer_";
//    public static final String classicBetResultPayoffByTableId = "classicBetResultPayoff_";
    public static final String updateSpotByGameName = "updateSpot_";
    public static final String updateCapLimit = "update_cap_limit_";
    public static final String addVariantIdList = "add_variantid_list";
    public static final String game = "game_";
    public static final String rl_last_dealt_spot = "rl_last_dealt_spot_";
    public static final String last_10_result = "last_10_result_";
    public static final String jokerByTableId = "joker_";
    public static final String currencyByCasinoId = "currency_";
    public static final String variants = "variant_";
    public static final String classicBetLimits = "classicBetLimits_";
    public static final String backLayBetLimits = "backLayBetLimits_";
    public static final String commonWalletFundTransferURL = "cw_url_fund_transfer_";
    public static final String commonWalletBalanceURL = "cw_url_balance_";
    public static final String grooveURL = "groove_url_";
    public static final String wacURL = "wac_url_";

    public static final String qTechURL = "qtech_url_";
    public static final String qTech_launch_passkey = "qTech_launch_passkey_";
    public static final String qtech_ipaddress = "qtech_ipaddress";
    public static final String partnerUsername = "partner_username_";
    public static final String partnerPassword = "partner_password_";
    public static final String pnCode = "pn_code_";
    public static final String pools = "pools_";
    public static final String poolRoom = "pool_room_";
    public static final String poolPlayerCount = "pool_player_count_";
    public static final String avatar = "avatar_";
    public static final String player_count = "player_count_";
    public static final String cap_limit = "cap_limit_";
    public static final String all_spot_table_max_per_false = "all_spot_table_max_per_false_";
    public static final String all_spot_table_max_per_true = "all_spot_table_max_per_true_";
    public static final String game_bet_amount = "game_bet_amount#";
    public static final String game_user_bet_amount = "game_user_bet_amount_";
    public static final String player_session = "player_session_";
    public static final String last_game_results = "last_game_results_";
    public static final String last_AB_percentage = "last_AB_percentage_";
    public static final String shuffle_status = "shuffle_status_";
    public static final String action_timer = "action_timer_";
    public static final String private_key = "private_key_";
    public static final String bcURL = "bc_url_";

    public static final String difference_enabled = "difference_enabled_";

    public static final String current_game_id = "current_game_id_";

//    @Autowired
//    KafkaHelper kafkaHelper;
//    @Autowired
//    GatewayService gatewayService;

//    @Value("${kafka.player.count.update}")
//    String playerCountUpdateSenderTopic;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisTemplate<String, String> stringTemplate;
    @Autowired
    private RedisTemplate<String, Object> objectTemplate;

    public void deleteStringKey(String key) {
        stringTemplate.delete(key);
    }

    public void deleteAll(String prefixKey) {
        Set<String> keys = stringTemplate.keys(prefixKey+ "*");

        for (String key : keys) {
            deleteStringKey(key);
        }
    }
    public Set<String> keys(String prefixKey) {
        return objectTemplate.keys(prefixKey+ "*");
    }

    public Boolean stringKeyExists(String key) {
        return stringTemplate.hasKey(key);
    }

    public Boolean objectKeyExists(String key) {
        return objectTemplate.hasKey(key);
    }

    public void set(String key, String value) {
        stringTemplate.opsForValue().set(key, value);
    }

    public String get(String key) {
        return stringTemplate.opsForValue().get(key);
    }

    public void set(String key, Object value) {
        objectTemplate.opsForValue().set(key, value);
    }

    public void deleteList(String key) {
        redisTemplate.delete(key);
    }

    public void saveListObject(String key, Object object) {
        String json = FunctionHelper.Serialize(object, true);
        redisTemplate.opsForList().rightPush(key, json);
    }

    public void saveListObject(String key, String string) {
        redisTemplate.opsForList().rightPush(key, string);
    }

    public List<String> getListObject(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public void setInt(String key, int value) {
        set(key, String.valueOf(value));
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public void setDouble(String key, double value) {
        set(key, String.valueOf(value));
    }

    public double getDouble(String key) {
        try {
            return Double.parseDouble(get(key));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public <T> T get(String key, Class<T> cls) {
        try {
            return cls.cast(objectTemplate.opsForValue().get(key));
        } catch (ClassCastException e) {
            return null;
        }
    }

    public void setObject(String key, Object object) {
        set(key, FunctionHelper.Serialize(object));
    }

    public <T> T getObject(String key, Class<T> cls) {
        T value = null;
        String jsonString = get(key);
        if (jsonString != null) {
            value = FunctionHelper.Deserialize(jsonString, cls);
        }

        return value;
    }

    public void setAsJson(String key, Object value) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(value);
            set(key, jsonString);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            log.info(key + " Catch_Exception {} ", value);
        }
    }

    public <T> List<T> getListFromJson(String key, Class<T> cls) {

        String jsonString = get(key);

        Gson gson = new Gson();

        Type type = TypeToken.getParameterized(List.class,cls).getType();
        return gson.fromJson(jsonString, type);
    }

    public HashMap<String, HashMap<String, Object>> getBetLimits(String key, String casinoId, String tableId, String currency) {
        HashMap<String, HashMap<String, Object>> values = getObject(key + casinoId + "_" + tableId +"_" + currency, HashMap.class);
        if(values == null) {
            values = getObject(key + "default" + "_" + tableId +"_" + currency, HashMap.class);
        }

        return values;
    }

    public void deletePlayerCountByTableId(String tableId) {
        deleteAll(player_count + tableId);
    }

    public String playerCountKey(String casinoId, String tableId) {
        return  player_count + tableId + "_" + casinoId;
    }

    public int setPlayerCount(SocketStatus socketStatus) {
        log.info("player count message in setPlayerCount .{}",socketStatus);
        String casinoId = socketStatus.getCasinoId();
        String tableId = socketStatus.getTableId();
        boolean isIncrement = socketStatus.getStatus().equals("join");

        String key = playerCountKey(casinoId, tableId);
        int count = getInt(key);

        if(isIncrement) {
            count++;
        } else {
            count--;
            if(count < 0) {
                count = 0;
            }
        }

        setInt(key, count);

//        if(socketStatus.getTopic() != null) {
//            sendPlayerCountUpdate(socketStatus.getTopic(), casinoId, tableId, count);
//        }
        return count;
    }

   */
/* private void sendPlayerCountUpdate(String topic, String casinoId, String tableId, int count) {
//        PlayerCountStatus playerCountStatus = PlayerCountStatus
//                .builder()
//                .casinoId(casinoId)
//                .tableId(tableId)
//                .players(count)
//                .build();

        HashMap<String, Object> map = new HashMap<>();
        map.put("tableId", tableId);
        map.put("players", count);

//        HashMap<String, Object> mapResponse = new HashMap<>();
//        mapResponse.put("code", 5038);
//        mapResponse.put("status", "TABLE_PLAYER_COUNT");
//        mapResponse.put("result", map);

//        String jsonString = FunctionHelper.getJsonString(mapResponse);
//
//        CasinoTableMessage gameCasinoTableMessage = CasinoTableMessage
//                .builder()
//                .casinoId(casinoId)
//                .tableId(tableId)
//                .jsonString(jsonString)
//                .build();
//
//        kafkaHelper.send(topic, FunctionHelper.getJsonString(gameCasinoTableMessage));

        String jsonString = CasinoTableMessage.getJsonString(casinoId, tableId, StatusCode.TABLE_PLAYER_COUNT, map);
        String lobbyJsonString = CasinoTableMessage.getJsonString(casinoId, "lobby", StatusCode.TABLE_PLAYER_COUNT, map);

        kafkaTemplate.send(topic, jsonString);
        kafkaTemplate.send("LOBBY_CASINO_TABLE_MESSAGE", lobbyJsonString);

//        kafkaHelper.casinoTableMessage(casinoId, tableId, StatusCode.TABLE_PLAYER_COUNT, map);
//        kafkaHelper.lobbyCasinoTableMessage(casinoId, StatusCode.TABLE_PLAYER_COUNT, map);

//        CasinoTableMessage lobbyCasinoTableMessage = CasinoTableMessage
//                .builder()
//                .casinoId(casinoId)
//                .tableId("lobby")
//                .jsonString(jsonString)
//                .build();
//
//        kafkaHelper.send("LOBBY_CASINO_TABLE_MESSAGE", FunctionHelper.getJsonString(lobbyCasinoTableMessage));

        //        gatewayService.playerCountStatusByCasino(casinoId, tableId, count);
    } *//*


    public int playerCount(String casinoId, String tableId) {
        String key = playerCountKey(casinoId, tableId);
        return getInt(key);
    }

//    public void deletePlayerCount(String topic, String tableId) {
//        String key = player_count + tableId + "_";
//        deleteAll(key);
//
//        sendPlayerCountUpdate(topic, null, tableId, 0);
//    }


    public String getLast10Results(String tableId) {
        return get(RedisKeyValue.last_10_result + tableId);
    }

    public void setLast10Results(String tableId, List<String> lastResults) {
        setLast10Results(tableId, String.join(",", lastResults));
    }

    public void setLast10Results(String tableId, String result) {
        set(RedisKeyValue.last_10_result + tableId, result);
    }

    public void setCasinoCurrencyValue(String casinoId, String currency, double base, double dollar) {
        setObject(RedisKeyValue.casino_currency_value + casinoId + "_" + currency,
                CasinoCurrencyData.builder()
                        .base(base)
                        .dollar(dollar)
                        .build());
    }

    public CasinoCurrencyData getCasinoCurrencyValue(String casinoId, String currency) {
        return getObject(RedisKeyValue.casino_currency_value + casinoId + "_" + currency, CasinoCurrencyData.class);
    }

    public void setPlayerSession(long playerId, Object object) {
        setObject(RedisKeyValue.player_session + playerId, object);
    }

    public <T> T getPlayerSession(long playerId, Class<T> cls) {
        return getObject(RedisKeyValue.player_session + playerId, cls);
    }

    public void deletePlayerSession(long playerId) {
        deleteStringKey(RedisKeyValue.player_session + playerId);
    }

    public void setLastWinResults(String tableId, Object object) {
        setObject(RedisKeyValue.last_game_results + tableId, object);
    }

    public <T> T getLastWinResults(String tableId, Class<T> cls) {
        return getObject(RedisKeyValue.last_game_results + tableId, cls);
    }

    public String getCommonWalletFundTransferURL(String casinoId) {
        return get(RedisKeyValue.commonWalletFundTransferURL + casinoId);
    }

    public String getGrooveURL(String casinoId) {
        return get(RedisKeyValue.grooveURL + casinoId);
    }

    public String getWACURL(String casinoId) {
        return get(RedisKeyValue.wacURL + casinoId);
    }

    public String getQTechURL(String casinoId) {
        return get(RedisKeyValue.qTechURL + casinoId);
    }

    public String getQTechLaunchPasskey(String casinoId) {
        return get(RedisKeyValue.qTech_launch_passkey + casinoId);
    }

    public String getPartnerUsername(String casinoId) {
        return get(RedisKeyValue.partnerUsername + casinoId);
    }

    public String getPartnerPassword(String casinoId) {
        return get(RedisKeyValue.partnerPassword + casinoId);
    }

    public String getPnCode(String casinoId) {
        return get(RedisKeyValue.pnCode + casinoId);
    }

    public String getCommonWalletBalanceURL(String casinoId) {
        return get(RedisKeyValue.commonWalletBalanceURL + casinoId);
    }

    public void setABPercentage(String tableId, String percentageValue) {
        set(RedisKeyValue.last_AB_percentage + tableId, percentageValue);
    }

    public HashMap<String, Object> getABPercentage(String tableId) {
        String percentageValue = get(RedisKeyValue.last_AB_percentage + tableId);

        HashMap<String, Object> percentageResults = new HashMap<>();

        if(percentageValue != null) {
            String[] values = percentageValue.split("#");
            percentageResults.put("ANDAR", Double.parseDouble(values[0]));
            percentageResults.put("BAHAR", Double.parseDouble(values[1]));
        }

        return percentageResults;
    }

    public void setCapLimitTableMaxByPercentageFalse(int variantId, String casinoId, String tableId, String currency, int betLimitId, double tableMax) {
        setDouble(RedisKeyValue.all_spot_table_max_per_false + variantId + "_" + casinoId + "_" + tableId + "_" + currency + "_" + betLimitId, tableMax);
    }

    public void setCapLimitTableMaxByPercentageTrue(int variantId, String casinoId, String tableId, int betLimitId, double tableMax) {
        setDouble(RedisKeyValue.all_spot_table_max_per_true + variantId + "_" + casinoId + "_" + tableId + "_" + betLimitId, tableMax);
    }

    public double getAllSpotTableMax(int variantId, String casinoId, String tableId, String currency, int betLimitId, double tableMax) {
        String key = RedisKeyValue.all_spot_table_max_per_false + variantId + "_" + casinoId + "_" + tableId + "_" + currency + "_" + betLimitId;

        if(stringKeyExists(key)) {
            return getDouble(key);
        }

        key = RedisKeyValue.all_spot_table_max_per_false + variantId + "_" + "All_" + tableId + "_" + currency + "_" + betLimitId;

        if(stringKeyExists(key)) {
            return getDouble(key);
        }

        double max = 0;

        key = RedisKeyValue.all_spot_table_max_per_true + variantId + "_" + casinoId + "_" + tableId + "_" + betLimitId;

        if(stringKeyExists(key)) {
            max = getDouble(key);
        } else {

            key = RedisKeyValue.all_spot_table_max_per_true + variantId + "_" + "All_" + tableId + "_" + betLimitId;

            if (stringKeyExists(key)) {
                max = getDouble(key);
            } else {

                key = RedisKeyValue.all_spot_table_max_per_true + variantId + "_" + "All_" + tableId + "_0";

                if (stringKeyExists(key)) {
                    max = getDouble(key);
                }
            }
        }

        if(max > 0) {
            return FunctionHelper.getBetAmountFromPercentage(tableMax, max);
        }

        return tableMax;
    }

    public boolean checkAllSpotTableMax(String casinoId, String tableId) {
        if(tableId.equals("Rou1hq4jo2etrjg3")) {
            int j = 0;
        }

        String capLimitKey = RedisKeyValue.cap_limit + casinoId + "_" + tableId;

        boolean checkAllSpotTableMax = false;

        if (stringKeyExists(capLimitKey)) {
            String value = get(capLimitKey);
            if (value.equals("0")) {
                checkAllSpotTableMax = true;
            }
        }

        return checkAllSpotTableMax;
    }

    public void setShuffleStatus(String tableId, String value) {
        set(RedisKeyValue.shuffle_status + tableId, value);
    }

    public String getShuffleStatus(String tableId) {
        return get(RedisKeyValue.shuffle_status + tableId);
    }

    public void deleteShuffleStatus(String tableId) {
        deleteStringKey(RedisKeyValue.shuffle_status + tableId);
    }

    public boolean isShuffleStatusExists(String tableId) {
        return stringKeyExists(RedisKeyValue.shuffle_status + tableId);
    }


    public void setActionTimer(String tableId, ActionTimerValue value) {
        setObject(RedisKeyValue.action_timer + tableId, value);
    }

    public ActionTimerValue getActionTimer(String tableId) {
        return getObject(RedisKeyValue.action_timer + tableId, ActionTimerValue.class);
    }

    public void deleteActionTimer(String tableId) {
        deleteStringKey(RedisKeyValue.action_timer + tableId);
    }

    public boolean isActionTimerExists(String tableId) {
        return stringKeyExists(RedisKeyValue.action_timer + tableId);
    }

    public String getBCURL(String casinoId) {
        return get(RedisKeyValue.bcURL + casinoId);
    }
    
    public String getBCPrivateKey(String casinoId) {
        return get(RedisKeyValue.private_key + casinoId);
    }
    
    public void setDifferenceEnabled(String tableId, String value) {
        set(RedisKeyValue.difference_enabled + tableId, value);
    }

    public String getDifferenceEnabled(String tableId) {
        return get(RedisKeyValue.difference_enabled + tableId);
    }
}*/
