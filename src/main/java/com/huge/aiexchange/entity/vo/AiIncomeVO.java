package com.huge.aiexchange.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class AiIncomeVO {

    private Integer income; // 总收益
    private Double yieldRate; // 收益率
    private Integer positionCount; // 持仓数量
    private Integer profitCount; // 盈利数量
    private List<StockPositionVO> positionDetails; // 持仓明细

    // 股票持仓明细类
    @Data
    public static class StockPositionVO {
        private String stockCode; // 股票代码
        private String stockName; // 股票名称
        private Integer position; // 持仓数量
        private Double profit; // 收益
        private Double profitRate; // 收益率
    }


}
