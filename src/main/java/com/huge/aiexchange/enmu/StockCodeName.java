package com.huge.aiexchange.enmu;

public enum StockCodeName {

    BTC(0, "BTC"),
    USDT(1, "USDT");

    Integer code;
    String name;

    StockCodeName(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(Integer code) {
        for (StockCodeName value : StockCodeName.values()) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return null;
    }

}
