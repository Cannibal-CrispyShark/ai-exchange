package com.huge.aiexchange.service.inter;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * AI交易助手接口
 * 包含获取模型ID的方法
 */
public interface AiTradeAssistant {

    /**
     * 获取AI回答
     * @param modelId 模型ID
     * @param userMessage 用户消息
     * @return AI回答
     */
    @SystemMessage("你是一个AI交易助手，可以帮助用户进行股票交易。\n" +
            "你可以使用buyStock和sellStock工具来买卖股票。\n" +
            "在调用工具时，你的模型ID会自动传入。")
    String getAnswer(@MemoryId Integer modelId, @UserMessage String userMessage);

    /**
     * 获取模型ID
     * @return 模型ID
     */
    Integer getModelId();

}
