package com.huge.aiexchange.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huge.aiexchange.constant.SystemConstants;
import com.huge.aiexchange.entity.pojo.AiModelInfo;
import com.huge.aiexchange.entity.pojo.AiPosition;
import com.huge.aiexchange.entity.pojo.StockFuture;
import com.huge.aiexchange.entity.vo.AiDecisionVO;
import com.huge.aiexchange.enums.RiskPreferenceEnum;
import com.huge.aiexchange.enums.StockCodeEnum;
import com.huge.aiexchange.mapper.AiModelInfoMapper;
import com.huge.aiexchange.mapper.AiPositionMapper;
import com.huge.aiexchange.mapper.StockFutureMapper;
import com.huge.aiexchange.service.inter.AiTradeAssistant;
import com.huge.aiexchange.service.inter.AssistantInter;
import com.huge.aiexchange.tool.AiTradeTool;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
    private AiPositionMapper aiPositionMapper;

    @Resource
    private ChatModel qwenChatModel;

    @Resource
    private AiTradeTool aiTradeTool;

    @Resource
    private AlphaVantageService alphaVantageService;

    @Resource(name = "CustomChatAssistant")
    private AssistantInter chatAssistant;

    @Resource(name = "qwenMaxAssistant")
    private AiTradeAssistant qwenMaxAssistant;

    @Resource(name = "qianfanAssistant")
    private AiTradeAssistant qianfanAssistant;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Prompt模板缓存
    private String promptTemplate;

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
     * @param riskPreference 风险偏好代码
     * @return 投资决策结果
     */
    public InvestmentDecisionResult makeInvestmentDecision(Integer modelId, String riskPreference) {
        try {
            // 1. 获取AI模型信息
            AiModelInfo aiModel = aiModelInfoMapper.selectById(modelId);
            if (aiModel == null) {
                return new InvestmentDecisionResult(false, "AI模型不存在", null);
            }

            // 2. 获取风险偏好
            RiskPreferenceEnum riskPref = RiskPreferenceEnum.getByCode(riskPreference);

            // 3. 获取经典股票特征数据
            List<StockFeatureInfo> stockFeatures = getStockFeatures();

            // 4. 获取当前持仓数据
            List<AiPosition> positions = aiPositionMapper.selectByModelId(modelId);

            // 5. 构建Prompt（根据风险偏好和持仓数据）
            String prompt = buildStructuredPrompt(aiModel, stockFeatures, riskPref, positions);

            // 6. 调用AI进行决策
            String aiResponse = getAssistant(aiModel.getModelName()).getAnswer(modelId, prompt);

            // 7. 解析AI的JSON响应
            AiDecisionVO decisionVO = parseAiResponse(aiResponse);

            // 8. 执行交易决策
            if (decisionVO.getTradeDecisions() != null) {
                executeTradeDecisions(modelId, aiModel.getModelName(), decisionVO.getTradeDecisions());
            }

            return new InvestmentDecisionResult(true, "投资决策完成", decisionVO);

        } catch (Exception e) {
            return new InvestmentDecisionResult(false, "决策过程出错: " + e.getMessage(), null);
        }
    }

    /**
     * 选择模型
     */
    private AiTradeAssistant getAssistant(String modelName){
        if (modelName.contains("ERNIE-5.0-Thinking-Preview")) {
            return qianfanAssistant;
        } else {
            // 默认使用qwenMaxAssistant
            return qwenMaxAssistant;
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
                    message = aiTradeTool.buyStock(modelId, modelName, decision.getStockName(), 
                            decision.getStockCode(), decision.getAmount());
                    result = message.contains("成功");
                } else if ("SELL".equalsIgnoreCase(decision.getAction())) {
                    message = aiTradeTool.sellStock(modelId, modelName, decision.getStockName(), 
                            decision.getStockCode(), decision.getAmount());
                    result = message.contains("成功");
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
     * 构建持仓数据字符串
     * @param positions 持仓列表
     * @return 格式化后的持仓数据字符串
     */
    private String buildPositionData(List<AiPosition> positions) {
        if (positions == null || positions.isEmpty()) {
            return "当前无持仓\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("股票代码 | 股票名称 | 持仓数量 | 平均成本 | 已实现收益\n");
        sb.append("---------|----------|----------|----------|----------\n");
        
        for (AiPosition position : positions) {
            sb.append(String.format("%-8s | %-8s | %8d | %8s | %10s\n",
                position.getStockCode(),
                position.getStockName(),
                position.getPosition(),
                position.getAverageCost() != null ? "$" + position.getAverageCost() : "$0.00",
                position.getProfit() != null ? "$" + position.getProfit() : "$0.00"
            ));
        }
        
        return sb.toString();
    }

    /**
     * 构建股票特征数据字符串
     * @param stockFeatures 股票特征列表
     * @return 格式化后的股票特征数据字符串
     */
    private String buildStockFeaturesData(List<StockFeatureInfo> stockFeatures) {
        StringBuilder sb = new StringBuilder();
        
        for (StockFeatureInfo info : stockFeatures) {
            StockCodeEnum stock = info.getStock();
            StockFuture future = info.getFuture();

            sb.append("\n=== ").append(stock.getStockName()).append(" (").append(stock.getStockCode()).append(") ===\n");
            sb.append("- 收盘价: $").append(future.getClose()).append("\n");
            sb.append("- 20日移动平均线: ").append(future.getMa20d()).append("\n");
            sb.append("- 60日移动平均线: ").append(future.getMa60d()).append("\n");
            sb.append("- 趋势位置(收盘价/60日MA): ").append(future.getTrendPosition()).append("\n");
            sb.append("- 5日收益率: ").append(future.getReturn5d() != null ? future.getReturn5d() + "%" : "N/A").append("\n");
            sb.append("- 20日收益率: ").append(future.getReturn20d() != null ? future.getReturn20d() + "%" : "N/A").append("\n");
            sb.append("- 20日波动率: ").append(future.getVolatility20d()).append("\n");
            sb.append("- 250日价格分位数: ").append(future.getPricePercentile250d()).append("\n");
            sb.append("- 100日价格分位数: ").append(future.getPricePercentile100d()).append("\n");
            sb.append("- 20日内最高价: $").append(future.getHighest20d()).append("\n");
            sb.append("- 是否创20日新高: ").append(future.getIsNew20dHigh() != null && future.getIsNew20dHigh() ? "是" : "否").append("\n");
        }
        
        return sb.toString();
    }

    /**
     * 获取风险偏好指导
     * @param riskPreference 风险偏好
     * @return 投资指导字符串
     */
    private String getRiskGuidance(RiskPreferenceEnum riskPreference) {
        StringBuilder sb = new StringBuilder();
        
        switch (riskPreference) {
            case CONSERVATIVE:
                sb.append("作为保守型投资者，你应该：\n");
                sb.append("- 优先保护本金，避免大额亏损\n");
                sb.append("- 选择波动性低、基本面稳健的股票\n");
                sb.append("- 避免追高，等待回调后再买入\n");
                sb.append("- 设置严格的止损点（如-5%）\n");
                sb.append("- 保持较高的现金比例（30%-50%）\n");
                sb.append("- 优先考虑分红稳定的蓝筹股\n");
                break;
            case MODERATE:
                sb.append("作为稳健型投资者，你应该：\n");
                sb.append("- 平衡风险与收益，适度承担风险\n");
                sb.append("- 关注成长性和价值性的平衡\n");
                sb.append("- 分散投资，不把所有资金投入单一股票\n");
                sb.append("- 设置合理的止损点（如-10%）\n");
                sb.append("- 保持适度的现金比例（20%-30%）\n");
                sb.append("- 结合技术分析和基本面分析做决策\n");
                break;
            case AGGRESSIVE:
                sb.append("作为激进型投资者，你应该：\n");
                sb.append("- 追求高收益，愿意承担较高风险\n");
                sb.append("- 关注高成长性股票，如科技股\n");
                sb.append("- 敢于在趋势确立时追涨\n");
                sb.append("- 设置较宽松的止损点（如-15%）\n");
                sb.append("- 保持较低的现金比例（10%-20%）\n");
                sb.append("- 积极参与市场热点，把握波段机会\n");
                break;
        }
        
        return sb.toString();
    }

    /**
     * 加载Prompt模板
     * @return Prompt模板字符串
     */
    private String loadPromptTemplate() {
        if (promptTemplate != null) {
            return promptTemplate;
        }
        
        try {
            ClassPathResource resource = new ClassPathResource("prompts/investment-decision.txt");
            promptTemplate = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return promptTemplate;
        } catch (IOException e) {
            // 如果加载失败，使用默认模板
            return getDefaultPromptTemplate();
        }
    }

    /**
     * 获取默认Prompt模板（当文件加载失败时使用）
     * @return 默认Prompt模板
     */
    private String getDefaultPromptTemplate() {
        return "你是一个专业的股票投资决策专家。请根据以下信息做出投资决策。\n\n" +
               "【你的账户信息】\n" +
               "- 模型名称: {modelName}\n" +
               "- 当前余额: ${deposit}\n" +
               "- 投资风格: {riskPreferenceName}\n" +
               "- 风险偏好描述: {riskPreferenceDesc}\n\n" +
               "【当前持仓情况】\n" +
               "{positionData}\n" +
               "【投资指导】\n" +
               "{riskGuidance}\n" +
               "【可选股票及其特征数据】\n" +
               "{stockFeatures}\n" +
               "请做出投资决策。";
    }

    /**
     * 构建结构化Prompt（根据风险偏好和持仓数据）
     * @param aiModel AI模型信息
     * @param stockFeatures 股票特征列表
     * @param riskPreference 风险偏好
     * @param positions 持仓列表
     * @return Prompt字符串
     */
    private String buildStructuredPrompt(AiModelInfo aiModel, List<StockFeatureInfo> stockFeatures, 
                                         RiskPreferenceEnum riskPreference, List<AiPosition> positions) {
        String template = loadPromptTemplate();
        
        // 替换模板中的变量
        String prompt = template
            .replace("{modelId}", aiModel.getId().toString())
            .replace("{modelName}", aiModel.getModelName())
            .replace("{deposit}", aiModel.getDeposit().toString())
            .replace("{riskPreferenceName}", riskPreference.getDisplayName())
            .replace("{riskPreferenceDesc}", riskPreference.getDescription())
            .replace("{positionData}", buildPositionData(positions))
            .replace("{riskGuidance}", getRiskGuidance(riskPreference))
            .replace("{stockFeatures}", buildStockFeaturesData(stockFeatures));
        
        return prompt;
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
