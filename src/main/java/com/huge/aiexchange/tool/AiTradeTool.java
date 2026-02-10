package com.huge.aiexchange.tool;

import com.huge.aiexchange.service.AiTradeService;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * AI交易工具类，作为AI进行买卖操作的工具
 * 使用@Tool注解让LangChain4j能够识别这些方法
 */
@Component
public class AiTradeTool {

    @Resource
    private AiTradeService aiTradeService;

    /**
     * 买入股票
     * @param modelId 模型ID，从AI上下文中自动获取
     * @param modelName 模型名称
     * @param stockName 股票名称
     * @param stockCode 股票代码
     * @param amount 买入数量
     * @return 操作结果描述，成功或失败的原因
     */
    @Tool(name = "买入股票的工具", value = "返回String，操作结果的描述信息")
    public String buyStock(Integer modelId, String modelName, String stockName, String stockCode, int amount) {
        try {
            return aiTradeService.buyStock(modelId, modelName, stockName, stockCode, amount);
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return "买入操作异常：" + e.getMessage();
        }
    }

    /**
     * 卖出股票
     * @param modelId 模型ID，从AI上下文中自动获取
     * @param modelName 模型名称
     * @param stockName 股票名称
     * @param stockCode 股票代码
     * @param amount 卖出数量
     * @return 操作结果描述，成功或失败的原因
     */
    @Tool(name = "卖出股票的工具", value = "返回String，操作结果的描述信息")
    public String sellStock(Integer modelId, String modelName, String stockName, String stockCode, int amount) {
        try {
            return aiTradeService.sellStock(modelId, modelName, stockName, stockCode, amount);

        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return "卖出操作异常：" + e.getMessage();
        }
    }

}
