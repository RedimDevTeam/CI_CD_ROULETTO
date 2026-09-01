package com.dowinn.rouletto.enums;

public enum SocketMessageStatus {
    CONFIRM_BET(300),
    SAVE_BET(301),
    BALANCE(302),
    WS_PING(500), AUTO_BET(303);

    public int getCode() {
        return code;
    }

    int code;

    SocketMessageStatus(int code) {
        this.code = code;
    }
}
