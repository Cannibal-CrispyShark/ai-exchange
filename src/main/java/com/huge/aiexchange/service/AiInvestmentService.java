package com.huge.aiexchange.service;

import com.huge.aiexchange.constant.SystemConstants;
import com.huge.aiexchange.entity.pojo.AiModelInfo;
import com.huge.aiexchange.entity.pojo.StockFuture;
import com.huge.aiexchange.enums.StockCodeEnum;
import com.huge.aiexchange.mapper.AiModelInfoMapper;
import com.huge.aiexchange.mapper.StockFutureMapper;
import com.huge.aiexchange.service.inter.AiTradeAssistant;
import com.huge.aiexchange.tool.AiTradeTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

/**
 * AI投资决策服务
 * 实现AI根据股票特征做投资决策的工作流
 */
@Service
public class AiInvestmentService {

    @Resource
    private AiModelInfoMapper aiModelInfoMapper;

    @Resource
    private StockFutureMapper stockFutureMapper;

    @Resource
    private ChatModel qwenChatModel;

    @Resource
    private AiTradeTool aiTradeTool;

    @Resource
    private AlphaVantageService alphaVantageService;

    /**
     * AI投资决策结果
     */
    public static class InvestmentDecisionResult {
        private String decision;      // 决策结果
        private String reason;        // 决策原因
        private boolean tradeExecuted; // 是否执行了交易

        public InvestmentDecisionResult(String decision, String reason, boolean tradeExecuted) {
            this.decision = decision;
            this.reason = reason;
            this.tradeExecuted = tradeExecuted;
        }

        // Getters and Setters
        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public boolean isTradeExecuted() { return tradeExecuted; }
        public void setTradeExecuted(boolean tradeExecuted) { this.tradeExecuted = tradeExecuted; }
    }

    /**
     * 执行AI投资决策
     * @param modelId AI模型ID
     * @return 投资决策结果
     */
    public InvestmentDecisionResult makeInvestmentDecision(Integer modelId) {
        // 1. 获取AI模型信息
        AiModelInfo aiModel = aiModelInfoMapper.selectById(modelId);
        if (aiModel == null) {
            throw new RuntimeException("AI模型不存在");
        }

        // 2. 获取经典股票特征数据
        List<StockFeatureInfo> stockFeatures = getStockFeatures();

        // 3. 构建Prompt
        String prompt = buildInvestmentPrompt(aiModel, stockFeatures);

        // 4. 创建AI服务并调用
        AiTradeAssistant assistant = AiServices.builder(AiTradeAssistant.class)
                .chatModel(qwenChatModel)
                .tools(aiTradeTool)
                .build();

        // 5. 调用AI进行决策
        String aiResponse = assistant.getAnswer(modelId, prompt);

        // 6. 解析AI响应，判断是否执行了交易
        boolean tradeExecuted = aiResponse.contains("买入") || aiResponse.contains("卖出") || 
                               aiResponse.contains("buy") || aiResponse.contains("sell");

        return new InvestmentDecisionResult(
                tradeExecuted ? "已执行交易" : "未执行交易",
                aiResponse,
                tradeExecuted
        );
    }

    /**
     * 获取经典股票特征数据
     * @return 股票特征信息列表
     */
    private List<StockFeatureInfo> getStockFeatures() {
        List<StockFeatureInfo> features = new ArrayList<>();

        for (StockCodeEnum stock : StockCodeEnum.values()) {
            StockFuture feature = stockFutureMapper.selectByStockCodeAndDate(stock.getStockCode(), SystemConstants.TODAY_MINUS_5);
            if (feature != null) {
                features.add(new StockFeatureInfo(stock, feature));
            }else{
                alphaVantageService.getBaseByAlpha(stock.getStockCode());
                feature = stockFutureMapper.selectByStockCodeAndDate(stock.getStockCode(), SystemConstants.TODAY_MINUS_5);
                features.add(new StockFeatureInfo(stock, feature));
            }
        }

        return features;
    }

    /**
     * 构建投资决策Prompt
     * @param aiModel AI模型信息
     * @param stockFeatures 股票特征列表
     * @return Prompt字符串
     */
    private String buildInvestmentPrompt(AiModelInfo aiModel, List<StockFeatureInfo> stockFeatures) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个专业的股票投资决策专家。请根据以下信息做出投资决策。\n\n");
        
        // AI账户信息
        prompt.append("【你的账户信息】\n");
        prompt.append("- 模型名称: ").append(aiModel.getModelName()).append("\n");
        prompt.append("- 当前余额: $").append(aiModel.getDeposit()).append("\n");
        prompt.append("- 投资风格: 稳健型，注重风险控制\n\n");
        
        // 股票特征数据
        prompt.append("【可选股票及其特征数据】\n");
        for (StockFeatureInfo info : stockFeatures) {
            StockCodeEnum stock = info.getStock();
            StockFuture future = info.getFuture();
            
            prompt.append("\n=== ").append(stock.getStockName()).append(" (").append(stock.getStockCode()).append(") ===\n");
            prompt.append("- 收盘价: $").append(future.getClose()).append("\n");
            prompt.append("- 20日移动平均线: ").append(future.getMa20d()).append("\n");
            prompt.append("- 60日移动平均线: ").append(future.getMa60d()).append("\n");
            prompt.append("- 趋势位置(收盘价/60日MA): ").append(future.getTrendPosition()).append("\n");
            prompt.append("- 5日收益率: ").append(future.getReturn5d() != null ? future.getReturn5d() + "%" : "N/A").append("\n");
            prompt.append("- 20日收益率: ").append(future.getReturn20d() != null ? future.getReturn20d() + "%" : "N/A").append("\n");
            prompt.append("- 20日波动率: ").append(future.getVolatility20d()).append("\n");
            prompt.append("- 250日价格分位数: ").append(future.getPricePercentile250d()).append("\n");
            prompt.append("- 100日价格分位数: ").append(future.getPricePercentile100d()).append("\n");
            prompt.append("- 20日内最高价: $").append(future.getHighest20d()).append("\n");
            prompt.append("- 是否创20日新高: ").append(future.getIsNew20dHigh() != null && future.getIsNew20dHigh() ? "是" : "否").append("\n");
        }
        
        prompt.append("\n【决策要求】\n");
        prompt.append("1. 分析每支股票的技术指标和趋势\n");
        prompt.append("2. 根据你的余额和风险偏好，决定是否买入或卖出\n");
        prompt.append("3. 如果决定交易，请使用买入或卖出工具执行交易\n");
        prompt.append("4. 请详细说明你的决策原因，包括：\n");
        prompt.append("   - 为什么选择这支股票\n");
        prompt.append("   - 技术指标如何支持你的决策\n");
        prompt.append("   - 风险控制考虑\n");
        prompt.append("   - 预期收益和风险\n\n");
        
        prompt.append("【可用工具】\n");
        prompt.append("- buyStock(modelId, modelName, stockName, stockCode, amount): 买入股票\n");
        prompt.append("- sellStock(modelId, modelName, stockName, stockCode, amount): 卖出股票\n\n");
        
        prompt.append("请做出你的投资决策，并详细说明原因。");
        
        return prompt.toString();
    }

    /**
     * 股票特征信息内部类
     */
    private static class StockFeatureInfo {
        private final StockCodeEnum stock;
        private final StockFuture future;

        public StockFeatureInfo(StockCodeEnum stock, StockFuture future) {
            this.stock = stock;
            this.future = future;
        }

        public StockCodeEnum getStock() { return stock; }
        public StockFuture getFuture() { return future; }
    }

}
