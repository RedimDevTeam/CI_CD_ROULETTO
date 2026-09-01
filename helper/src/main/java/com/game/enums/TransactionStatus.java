package com.game.enums;

import java.util.HashMap;
import java.util.Map;

public enum TransactionStatus {
    SUCCESS(0),
    INITIAL(1),

    INSUFICIENT_BALANCE(112),

    SYSTEM_ERROR(110),
    INVALID_REQUEST(109),
    FAILURE(2);
    private final int value;

    private static final Map<Integer, TransactionStatus> lookup = new HashMap<Integer, TransactionStatus>();
    static {
        for (TransactionStatus t : TransactionStatus.values()) {
            lookup.put(t.value(), t);
        }
    }

    TransactionStatus(int value) {
        this.value=value;
    }
    public static TransactionStatus of(int value){
        return lookup.get(value);
    }

    public int value() {
        return value;
    }
}
