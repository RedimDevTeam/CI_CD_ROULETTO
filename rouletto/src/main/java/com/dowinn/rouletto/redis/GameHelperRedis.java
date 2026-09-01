package com.dowinn.rouletto.redis;

public enum GameHelperRedis {

    GAME_BET_SPOT("gamebetspots_"),
    TABLE_CONFIG("tableconfig_"),
    PAST_RESULT("pastresult_"),
    BET_LIMIT("betlimit_"),
    CURRENCY_RATE("currency:");

    GameHelperRedis(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String name;
}
