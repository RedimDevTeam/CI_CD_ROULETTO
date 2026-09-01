package com.dowinn.rouletto.communication;

public enum StatusCode {
    CONNECTION_ESTABLISHED(0),
    SUCCESS(1),
    FAILED(2),
    SPOT_LIMITS(100),
    BET_NOT_ACCEPTED(101),
    INVALID_BET_LIMIT(102),
    BETS_CONFIRMED(103),
    BALANCE(104),
    UPDATE_BALANCE(105),
    BET_TIMER(106),
    RESULT_TIMER(107),
    CONNECTION_CONFIRMED_BET(108),
    UPDATE_JACKPOT(109),
    WS_ROULETTE_RESULT(110),
    GAME_CANCEL(111),
    NO_GAME_STARTED(112),
    IN_PROGRESS(113),
    ACCOUNT_BLOCKED(300),
    WS_PONG(301),
    SESSION_EXPIRED(302),
    GAME_COMPLETED(114);

    public int value() {
        return value;
    }

    int value;

    StatusCode(int value) {
        this.value = value;
    }

}
