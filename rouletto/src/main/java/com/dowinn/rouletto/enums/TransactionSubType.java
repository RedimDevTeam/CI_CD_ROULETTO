package com.dowinn.rouletto.enums;

public enum TransactionSubType {
    BET(300),
    WIN(302),
    LOSS(303),
    CANCEL(304);

    public int getStatus() {
        return status;
    }

    TransactionSubType(int status) {
        this.status = status;
    }

    int status;
}
