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
import com.huge.aiexchange.tool.AiTradeTool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI投资决策流式服务
 * 使用SSE流式返回投资决策过程和结果
 */
@Slf4j
@Service
public class AiInvestmentStreamService {

    @Resource
    private AiModelInfoMapper aiModelInfoMapper;

    @Resource
    private StockFutureMapper stockFutureMapper;

    @Resource
    private AiPositionMapper aiPositionMapper;

    @Resource
    private AiTradeTool aiTradeTool;

    @Resource
    private AlphaVantageService alphaVantageService;

    @Resource(name = "qwenMaxAssistant")
    private AiTradeAssistant qwenMaxAssistant;

    @Resource(name = "qianfanAssistant")
    private AiTradeAssistant qianfanAssistant;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Prompt模板缓存
    private String promptTemplate;

    // 存储emitter的连接状态
    private final ConcurrentHashMap<SseEmitter, Boolean> emitterStatus = new ConcurrentHashMap<>();

    /**
     * 执行AI投资决策（流式）
     *
     * @param modelId        AI模型ID
     * @param riskPreference 风险偏好代码
     * @param emitter        SSE发射器
     */
    public void makeInvestmentDecisionStream(Integer modelId, String riskPreference, SseEmitter emitter) {
        // 注册emitter并设置状态为活跃
        emitterStatus.put(emitter, true);
        
        // 设置emitter的完成、超时和错误回调
        emitter.onCompletion(() -> {
            log.info("SSE连接已完成");
            emitterStatus.put(emitter, false);
        });
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitterStatus.put(emitter, false);
        });
        emitter.onError((e) -> {
            log.error("SSE连接错误", e);
            emitterStatus.put(emitter, false);
        });
        
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 发送开始事件
                if (!sendEvent(emitter, "start", "{\"message\": \"开始投资决策分析...\"}")) return;

                // 2. 获取AI模型信息
                if (!sendEvent(emitter, "progress", "{\"message\": \"获取AI模型信息...\", \"progress\": 10}")) return;
                AiModelInfo aiModel = aiModelInfoMapper.selectById(modelId);
                if (aiModel == null) {
                    sendEvent(emitter, "error", "{\"message\": \"AI模型不存在\"}");
                    emitter.complete();
                    return;
                }

                // 3. 获取风险偏好
                if (!sendEvent(emitter, "progress", "{\"message\": \"加载风险偏好配置...\", \"progress\": 20}")) return;
                RiskPreferenceEnum riskPref = RiskPreferenceEnum.getByCode(riskPreference);

                // 4. 获取经典股票特征数据
                if (!sendEvent(emitter, "progress", "{\"message\": \"获取股票特征数据...\", \"progress\": 30}")) return;
                List<StockFeatureInfo> stockFeatures = getStockFeatures();

                // 5. 获取当前持仓数据
                if (!sendEvent(emitter, "progress", "{\"message\": \"获取当前持仓数据...\", \"progress\": 40}")) return;
                List<AiPosition> positions = aiPositionMapper.selectByModelId(modelId);

                // 6. 构建Prompt
                if (!sendEvent(emitter, "progress", "{\"message\": \"构建投资决策提示词...\", \"progress\": 50}")) return;
                String prompt = buildStructuredPrompt(aiModel, stockFeatures, riskPref, positions);
                
                // 检查prompt是否为空
                if (prompt == null || prompt.trim().isEmpty()) {
                    log.error("构建的Prompt为空");
                    sendEvent(emitter, "error", "{\"message\": \"提示词构建失败，请重试\"}");
                    emitter.complete();
                    return;
                }
                
                log.debug("构建的Prompt长度: {}", prompt.length());

                // 7. 调用AI进行决策（非流式，但分阶段返回）
                if (!sendEvent(emitter, "progress", "{\"message\": \"AI正在分析决策，请稍候...\", \"progress\": 60}")) return;
                
                // 模拟流式效果：分阶段返回思考过程
                simulateStreamingProcess(emitter);
                
                // 检查连接是否仍然活跃
                if (!isEmitterActive(emitter)) {
                    log.info("客户端已断开连接，停止处理");
                    return;
                }
                
                // 实际调用AI
                String aiResponse = getAssistant(aiModel.getModelName()).getAnswer(modelId, prompt);

                if (!sendEvent(emitter, "progress", "{\"message\": \"解析AI决策结果...\", \"progress\": 80}")) return;

                // 解析AI响应
                AiDecisionVO decisionVO = parseAiResponse(aiResponse);

                // 发送决策结果
                String decisionJson = objectMapper.writeValueAsString(decisionVO);
                if (!sendEvent(emitter, "decision", decisionJson)) return;

                // 执行交易决策
                if (!sendEvent(emitter, "progress", "{\"message\": \"执行交易决策...\", \"progress\": 90}")) return;
                if (decisionVO.getTradeDecisions() != null) {
                    executeTradeDecisions(modelId, aiModel.getModelName(), decisionVO.getTradeDecisions(), emitter);
                }

                // 发送完成事件
                sendEvent(emitter, "complete", "{\"message\": \"投资决策完成\", \"progress\": 100}");
                emitter.complete();

            } catch (Exception e) {
                log.error("投资决策流式处理失败", e);
                sendEvent(emitter, "error", "{\"message\": \"决策过程出错: " + escapeJson(e.getMessage()) + "\"}");
                emitter.completeWithError(e);
            } finally {
                // 清理emitter状态
                emitterStatus.remove(emitter);
            }
        });
    }

    /**
     * 检查emitter是否仍然活跃
     */
    private boolean isEmitterActive(SseEmitter emitter) {
        Boolean isActive = emitterStatus.get(emitter);
        return isActive != null && isActive;
    }

    /**
     * 模拟流式思考过程
     */
    private void simulateStreamingProcess(SseEmitter emitter) {
        String[] thinkingSteps = {
            "正在分析市场趋势...",
            "正在评估股票基本面...",
            "正在计算技术指标...",
            "正在评估风险收益比...",
            "正在制定投资策略...",
            "正在生成交易建议..."
        };

        for (String step : thinkingSteps) {
            // 检查连接是否仍然活跃
            if (!isEmitterActive(emitter)) {
                log.debug("客户端已断开连接，停止模拟思考过程");
                return;
            }
            
            try {
                Thread.sleep(500); // 模拟思考时间
                if (!sendEvent(emitter, "thinking", "{\"message\": \"" + escapeJson(step) + "\"}")) {
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * 发送SSE事件
     * @return 是否发送成功
     */
    private boolean sendEvent(SseEmitter emitter, String eventName, String data) {
        // 检查emitter是否仍然活跃
        Boolean isActive = emitterStatus.get(emitter);
        if (isActive == null || !isActive) {
            log.debug("SSE连接已断开，跳过发送事件: {}", eventName);
            return false;
        }
        
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            return true;
        } catch (org.springframework.web.context.request.async.AsyncRequestNotUsableException e) {
            // 客户端已断开连接
            log.debug("客户端已断开连接，停止发送事件: {}", eventName);
            emitterStatus.put(emitter, false);
            return false;
        } catch (IOException e) {
            log.error("发送SSE事件失败: {}", eventName, e);
            emitterStatus.put(emitter, false);
            return false;
        } catch (Exception e) {
            log.error("发送SSE事件时发生未知错误: {}", eventName, e);
            emitterStatus.put(emitter, false);
            return false;
        }
    }

    /**
     * 转义JSON字符串
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 选择模型
     */
    private AiTradeAssistant getAssistant(String modelName) {
        if (modelName.contains("qianfan")) {
            return qianfanAssistant;
        } else {
            return qwenMaxAssistant;
        }
    }

    /**
     * 解析AI响应
     */
    private AiDecisionVO parseAiResponse(String aiResponse) {
        try {
            String jsonStr = extractJsonFromResponse(aiResponse);
            return objectMapper.readValue(jsonStr, AiDecisionVO.class);
        } catch (Exception e) {
            AiDecisionVO fallback = new AiDecisionVO();
            fallback.setSummary("AI响应解析失败，原始响应: " + aiResponse);
            return fallback;
        }
    }

    /**
     * 从AI响应中提取JSON字符串
     */
    private String extractJsonFromResponse(String response) {
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        return response;
    }

    /**
     * 执行交易决策（流式）
     */
    private void executeTradeDecisions(Integer modelId, String modelName,
                                       List<AiDecisionVO.TradeDecision> tradeDecisions,
                                       SseEmitter emitter) {
        for (AiDecisionVO.TradeDecision decision : tradeDecisions) {
            // 检查连接是否仍然活跃
            if (!isEmitterActive(emitter)) {
                log.debug("客户端已断开连接，停止执行交易决策");
                return;
            }
            
            try {
                String message;
                boolean result = false;

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
                } else {
                    message = "未知操作: " + decision.getAction();
                }

                decision.setExecuted(result);
                decision.setExecutionMessage(message);

                // 发送交易执行事件
                String tradeResult = String.format(
                        "{\"action\": \"%s\", \"stockCode\": \"%s\", \"success\": %b, \"message\": \"%s\"}",
                        decision.getAction(),
                        decision.getStockCode(),
                        result,
                        escapeJson(message)
                );
                if (!sendEvent(emitter, "trade", tradeResult)) {
                    return;
                }

            } catch (Exception e) {
                decision.setExecuted(false);
                decision.setExecutionMessage("执行异常: " + e.getMessage());
                if (!sendEvent(emitter, "trade_error", "{\"message\": \"执行异常: " + escapeJson(e.getMessage()) + "\"}")) {
                    return;
                }
            }
        }
    }

    /**
     * 获取经典股票特征数据
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
     */
    private String buildPositionData(List<AiPosition> positions) {
        if (positions == null || positions.isEmpty()) {
            return "当前无持仓\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("股票代码 | 股票名称 | 持仓数量 | 平均成本 | 已实现收益\n");
        sb.append("---------|----------|----------|----------|----------\n");
        for (AiPosition position : positions) {
            if (position == null) continue;
            sb.append(String.format("%-8s | %-8s | %8s | %8s | %10s\n",
                    position.getStockCode() != null ? position.getStockCode() : "N/A",
                    position.getStockName() != null ? position.getStockName() : "N/A",
                    position.getPosition() != null ? position.getPosition() : 0,
                    position.getAverageCost() != null ? "$" + position.getAverageCost() : "$0.00",
                    position.getProfit() != null ? "$" + position.getProfit() : "$0.00"
            ));
        }
        return sb.toString();
    }

    /**
     * 构建股票特征数据字符串
     */
    private String buildStockFeaturesData(List<StockFeatureInfo> stockFeatures) {
        if (stockFeatures == null || stockFeatures.isEmpty()) {
            return "暂无股票特征数据\n";
        }
        
        StringBuilder sb = new StringBuilder();
        for (StockFeatureInfo info : stockFeatures) {
            if (info == null || info.getStock() == null || info.getFuture() == null) {
                continue;
            }
            StockCodeEnum stock = info.getStock();
            StockFuture future = info.getFuture();
            sb.append("\n=== ").append(stock.getStockName()).append(" (").append(stock.getStockCode()).append(") ===\n");
            sb.append("- 收盘价: $").append(future.getClose() != null ? future.getClose() : "N/A").append("\n");
            sb.append("- 20日移动平均线: ").append(future.getMa20d() != null ? future.getMa20d() : "N/A").append("\n");
            sb.append("- 60日移动平均线: ").append(future.getMa60d() != null ? future.getMa60d() : "N/A").append("\n");
            sb.append("- 趋势位置: ").append(future.getTrendPosition() != null ? future.getTrendPosition() : "N/A").append("\n");
            sb.append("- 5日收益率: ").append(future.getReturn5d() != null ? future.getReturn5d() + "%" : "N/A").append("\n");
            sb.append("- 20日收益率: ").append(future.getReturn20d() != null ? future.getReturn20d() + "%" : "N/A").append("\n");
            sb.append("- 20日波动率: ").append(future.getVolatility20d() != null ? future.getVolatility20d() : "N/A").append("\n");
        }
        return sb.toString();
    }

    /**
     * 获取风险偏好指导
     */
    private String getRiskGuidance(RiskPreferenceEnum riskPreference) {
        StringBuilder sb = new StringBuilder();
        
        // 如果riskPreference为null，默认使用稳健型
        if (riskPreference == null) {
            riskPreference = RiskPreferenceEnum.MODERATE;
        }
        
        switch (riskPreference) {
            case CONSERVATIVE:
                sb.append("作为保守型投资者，你应该：\n");
                sb.append("- 优先保护本金，避免大额亏损\n");
                sb.append("- 选择波动性低、基本面稳健的股票\n");
                sb.append("- 避免追高，等待回调后再买入\n");
                sb.append("- 设置严格的止损点（如-5%）\n");
                sb.append("- 保持较高的现金比例（30%-50%）\n");
                break;
            case MODERATE:
                sb.append("作为稳健型投资者，你应该：\n");
                sb.append("- 平衡风险与收益，适度承担风险\n");
                sb.append("- 关注成长性和价值性的平衡\n");
                sb.append("- 分散投资，不把所有资金投入单一股票\n");
                sb.append("- 设置合理的止损点（如-10%）\n");
                sb.append("- 保持适度的现金比例（20%-30%）\n");
                break;
            case AGGRESSIVE:
                sb.append("作为激进型投资者，你应该：\n");
                sb.append("- 追求高收益，愿意承担较高风险\n");
                sb.append("- 关注高成长性股票，如科技股\n");
                sb.append("- 敢于在趋势确立时追涨\n");
                sb.append("- 设置较宽松的止损点（如-15%）\n");
                sb.append("- 保持较低的现金比例（10%-20%）\n");
                break;
        }
        return sb.toString();
    }

    /**
     * 加载Prompt模板
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
            return getDefaultPromptTemplate();
        }
    }

    /**
     * 获取默认Prompt模板
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
     * 构建结构化Prompt
     */
    private String buildStructuredPrompt(AiModelInfo aiModel, List<StockFeatureInfo> stockFeatures,
                                         RiskPreferenceEnum riskPreference, List<AiPosition> positions) {
        String template = loadPromptTemplate();
        
        // 安全获取字段值，防止null
        String modelName = aiModel.getModelName() != null ? aiModel.getModelName() : "未知模型";
        String deposit = aiModel.getDeposit() != null ? aiModel.getDeposit().toString() : "0";
        String riskName = riskPreference != null ? riskPreference.getDisplayName() : "稳健型";
        String riskDesc = riskPreference != null ? riskPreference.getDescription() : "平衡风险收益";
        
        return template
                .replace("{modelName}", modelName)
                .replace("{deposit}", deposit)
                .replace("{riskPreferenceName}", riskName)
                .replace("{riskPreferenceDesc}", riskDesc)
                .replace("{positionData}", buildPositionData(positions))
                .replace("{riskGuidance}", getRiskGuidance(riskPreference))
                .replace("{stockFeatures}", buildStockFeaturesData(stockFeatures));
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

        public StockCodeEnum getStock() {
            return stock;
        }

        public StockFuture getFuture() {
            return future;
        }
    }
}
