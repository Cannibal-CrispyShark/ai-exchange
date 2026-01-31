package com.huge.aiexchange.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huge.aiexchange.constant.SystemConstants;
import com.huge.aiexchange.entity.pojo.AiModelInfo;
import com.huge.aiexchange.entity.pojo.StockFuture;
import com.huge.aiexchange.entity.vo.AiDecisionVO;
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

    @Resource
    private AiTradeAssistant assistant;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI投资决策结果
     */
    public static class InvestmentDecisionResult {
        private boolean success;
        private String message;
        private AiDecisionVO decisionData;

        public InvestmentDecisionResult(boolean success, String message, AiDecisionVO decisionData) {
            this.success = success;
            this.message = message;
            this.decisionData = decisionData;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public AiDecisionVO getDecisionData() { return decisionData; }
        public void setDecisionData(AiDecisionVO decisionData) { this.decisionData = decisionData; }
    }

    /**
     * 执行AI投资决策
     * @param modelId AI模型ID
     * @return 投资决策结果
     */
    public InvestmentDecisionResult makeInvestmentDecision(Integer modelId) {
        try {
            // 1. 获取AI模型信息
            AiModelInfo aiModel = aiModelInfoMapper.selectById(modelId);
            if (aiModel == null) {
                return new InvestmentDecisionResult(false, "AI模型不存在", null);
            }

            // 2. 获取经典股票特征数据
            List<StockFeatureInfo> stockFeatures = getStockFeatures();

            // 3. 构建Prompt（要求JSON格式输出）
            String prompt = buildStructuredPrompt(aiModel, stockFeatures);

            // 4. 调用AI进行决策
            String aiResponse = assistant.getAnswer(modelId, prompt);

            // 5. 解析AI的JSON响应
            AiDecisionVO decisionVO = parseAiResponse(aiResponse);

            // 6. 执行交易决策
            if (decisionVO.getTradeDecisions() != null) {
                executeTradeDecisions(modelId, aiModel.getModelName(), decisionVO.getTradeDecisions());
            }

            return new InvestmentDecisionResult(true, "投资决策完成", decisionVO);

        } catch (Exception e) {
            return new InvestmentDecisionResult(false, "决策过程出错: " + e.getMessage(), null);
        }
    }

    /**
     * 解析AI响应
     * @param aiResponse AI响应文本
     * @return 解析后的AiDecisionVO
     */
    private AiDecisionVO parseAiResponse(String aiResponse) {
        try {
            // 尝试提取JSON部分（AI可能在JSON前后添加了说明文字）
            String jsonStr = extractJsonFromResponse(aiResponse);
            return objectMapper.readValue(jsonStr, AiDecisionVO.class);
        } catch (Exception e) {
            // 如果解析失败，创建一个基础结构
            AiDecisionVO fallback = new AiDecisionVO();
            fallback.setSummary("AI响应解析失败，原始响应: " + aiResponse);
            return fallback;
        }
    }

    /**
     * 从AI响应中提取JSON字符串
     * @param response AI响应
     * @return JSON字符串
     */
    private String extractJsonFromResponse(String response) {
        // 查找第一个 { 和最后一个 }
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        
        return response;
    }

    /**
     * 执行交易决策
     * @param modelId 模型ID
     * @param modelName 模型名称
     * @param tradeDecisions 交易决策列表
     */
    private void executeTradeDecisions(Integer modelId, String modelName, List<AiDecisionVO.TradeDecision> tradeDecisions) {
        for (AiDecisionVO.TradeDecision decision : tradeDecisions) {
            try {
                boolean result = false;
                String message = "";

                if ("BUY".equalsIgnoreCase(decision.getAction())) {
                    result = aiTradeTool.buyStock(modelId, modelName, decision.getStockName(), 
                            decision.getStockCode(), decision.getAmount());
                    message = result ? "买入成功" : "买入失败";
                } else if ("SELL".equalsIgnoreCase(decision.getAction())) {
                    result = aiTradeTool.sellStock(modelId, modelName, decision.getStockName(), 
                            decision.getStockCode(), decision.getAmount());
                    message = result ? "卖出成功" : "卖出失败";
                } else if ("HOLD".equalsIgnoreCase(decision.getAction())) {
                    result = true;
                    message = "保持持仓";
                }

                decision.setExecuted(result);
                decision.setExecutionMessage(message);

            } catch (Exception e) {
                decision.setExecuted(false);
                decision.setExecutionMessage("执行异常: " + e.getMessage());
            }
        }
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
            } else {
                alphaVantageService.getBaseByAlpha(stock.getStockCode());
                feature = stockFutureMapper.selectByStockCodeAndDate(stock.getStockCode(), SystemConstants.TODAY_MINUS_5);
                if (feature != null) {
                    features.add(new StockFeatureInfo(stock, feature));
                }
            }
        }

        return features;
    }

    /**
     * 构建结构化Prompt（要求JSON格式输出）
     * @param aiModel AI模型信息
     * @param stockFeatures 股票特征列表
     * @return Prompt字符串
     */
    private String buildStructuredPrompt(AiModelInfo aiModel, List<StockFeatureInfo> stockFeatures) {
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

        prompt.append("\n【可用工具】\n");
        prompt.append("- buyStock(modelId, modelName, stockName, stockCode, amount): 买入股票\n");
        prompt.append("- sellStock(modelId, modelName, stockName, stockCode, amount): 卖出股票\n\n");

        prompt.append("【输出格式要求】\n");
        prompt.append("请严格按照以下JSON格式输出你的投资决策，不要添加任何其他说明文字：\n\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"决策总结，简要说明整体策略\",\n");
        prompt.append("  \"tradeDecisions\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"action\": \"BUY/SELL/HOLD\",\n");
        prompt.append("      \"stockCode\": \"股票代码\",\n");
        prompt.append("      \"stockName\": \"股票名称\",\n");
        prompt.append("      \"amount\": 交易数量,\n");
        prompt.append("      \"reason\": \"交易原因\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"marketAnalysis\": {\n");
        prompt.append("    \"overallTrend\": \"整体市场趋势判断\",\n");
        prompt.append("    \"stockAnalyses\": [\n");
        prompt.append("      {\n");
        prompt.append("        \"stockCode\": \"股票代码\",\n");
        prompt.append("        \"stockName\": \"股票名称\",\n");
        prompt.append("        \"technicalAnalysis\": \"技术分析\",\n");
        prompt.append("        \"trend\": \"趋势判断\",\n");
        prompt.append("        \"supportLevel\": \"支撑位\",\n");
        prompt.append("        \"resistanceLevel\": \"阻力位\"\n");
        prompt.append("      }\n");
        prompt.append("    ]\n");
        prompt.append("  },\n");
        prompt.append("  \"riskAssessment\": {\n");
        prompt.append("    \"riskLevel\": \"LOW/MEDIUM/HIGH\",\n");
        prompt.append("    \"riskDescription\": \"风险说明\",\n");
        prompt.append("    \"riskControlMeasures\": \"风控措施\",\n");
        prompt.append("    \"expectedReturn\": \"预期收益\",\n");
        prompt.append("    \"maxAcceptableLoss\": \"最大可承受损失\"\n");
        prompt.append("  }\n");
        prompt.append("}\n\n");

        prompt.append("【决策要求】\n");
        prompt.append("1. 分析每支股票的技术指标和趋势\n");
        prompt.append("2. 根据你的余额和风险偏好，决定是否买入、卖出或持有\n");
        prompt.append("3. action只能是BUY、SELL或HOLD之一\n");
        prompt.append("4. 如果决定交易（BUY/SELL），请同时调用相应的工具执行交易\n");
        prompt.append("5. 请详细填写每个字段，确保JSON格式正确\n");

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
