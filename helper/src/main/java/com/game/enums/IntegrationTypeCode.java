package com.game.enums;

import java.util.HashMap;
import java.util.Map;

public enum IntegrationTypeCode {

    NONE (0),
    EGAME(1),
    COMMON_WALLET(2),

    PAMS_WALLET(3);




    private static final Map<Integer, IntegrationTypeCode> valueMap = new HashMap<Integer, IntegrationTypeCode>();

    private final int value;

    IntegrationTypeCode(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    static {

        for (IntegrationTypeCode integrationTypeCode : IntegrationTypeCode.values()) {
            valueMap.put(integrationTypeCode.value, integrationTypeCode);
        }
    }

    public static IntegrationTypeCode byValue(int value) {
        return valueMap.get(value);
    }
}
