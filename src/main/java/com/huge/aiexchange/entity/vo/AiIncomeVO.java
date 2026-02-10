package com.huge.aiexchange.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class AiIncomeVO {

    private Integer income; // 总收益（取整）= 已实现收益 + 未实现收益
    private Double yieldRate; // 整体收益率
    private Integer positionCount; // 持仓股票种类数
    private Integer stockCount; // 总持仓股数
    private Double totalCost; // 总成本（平均成本 * 持仓数量）
    private Double realizedProfit; // 总已实现收益
    private Double unrealizedProfit; // 总未实现收益
    private List<StockPositionVO> positionDetails; // 持仓明细

    // 股票持仓明细类
    @Data
    public static class StockPositionVO {
        private String stockCode; // 股票代码
        private String stockName; // 股票名称
        private Integer position; // 持仓数量
        private Double averageCost; // 平均成本
        private Double currentPrice; // 当前价格
        private Double returnRate; // 收益率（当前价格 / 平均成本）
        private Double realizedProfit; // 已实现收益
        private Double unrealizedProfit; // 未实现收益
        private Double totalProfit; // 总收益 = 已实现 + 未实现
    }


}
