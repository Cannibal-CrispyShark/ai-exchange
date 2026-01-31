package com.huge.aiexchange.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * AI投资决策结构化输出VO
 */
@Data
public class AiDecisionVO {

    /**
     * 决策总结
     */
    private String summary;

    /**
     * 交易决策列表
     */
    private List<TradeDecision> tradeDecisions;

    /**
     * 市场分析
     */
    private MarketAnalysis marketAnalysis;

    /**
     * 风险评估
     */
    private RiskAssessment riskAssessment;

    /**
     * 交易决策
     */
    @Data
    public static class TradeDecision {
        /**
         * 操作类型：BUY/SELL/HOLD
         */
        private String action;

        /**
         * 股票代码
         */
        private String stockCode;

        /**
         * 股票名称
         */
        private String stockName;

        /**
         * 交易数量
         */
        private Integer amount;

        /**
         * 交易原因
         */
        private String reason;

        /**
         * 是否执行成功
         */
        private Boolean executed;

        /**
         * 执行结果消息
         */
        private String executionMessage;
    }

    /**
     * 市场分析
     */
    @Data
    public static class MarketAnalysis {
        /**
         * 整体市场趋势
         */
        private String overallTrend;

        /**
         * 关注的股票分析
         */
        private List<StockAnalysis> stockAnalyses;
    }

    /**
     * 个股分析
     */
    @Data
    public static class StockAnalysis {
        /**
         * 股票代码
         */
        private String stockCode;

        /**
         * 股票名称
         */
        private String stockName;

        /**
         * 技术分析
         */
        private String technicalAnalysis;

        /**
         * 趋势判断
         */
        private String trend;

        /**
         * 支撑位
         */
        private String supportLevel;

        /**
         * 阻力位
         */
        private String resistanceLevel;
    }

    /**
     * 风险评估
     */
    @Data
    public static class RiskAssessment {
        /**
         * 风险等级：LOW/MEDIUM/HIGH
         */
        private String riskLevel;

        /**
         * 风险说明
         */
        private String riskDescription;

        /**
         * 风控措施
         */
        private String riskControlMeasures;

        /**
         * 预期收益
         */
        private String expectedReturn;

        /**
         * 最大可承受损失
         */
        private String maxAcceptableLoss;
    }

}
