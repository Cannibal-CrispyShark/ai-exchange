package com.huge.aiexchange.enums;

import lombok.Getter;

/**
 * 经典股票枚举类
 * 定义常用的股票代码和名称
 */
@Getter
public enum StockCodeEnum {

    APPLE("AAPL", "Apple Inc.");


    /**
     * 股票代码
     */
    private final String stockCode;

    /**
     * 股票英文名称
     */
    private final String stockName;

    StockCodeEnum(String stockCode, String stockName) {
        this.stockCode = stockCode;
        this.stockName = stockName;
    }

    /**
     * 根据股票代码获取枚举
     * @param stockCode 股票代码
     * @return 枚举对象，如果不存在返回null
     */
    public static StockCodeEnum getByCode(String stockCode) {
        for (StockCodeEnum stock : values()) {
            if (stock.getStockCode().equals(stockCode)) {
                return stock;
            }
        }
        return null;
    }

    /**
     * 获取股票名称（中文优先）
     * @return 股票名称
     */
    public String getStockName() {
        return stockName;
    }

}
