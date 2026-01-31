package com.huge.aiexchange.configuration;


import com.huge.aiexchange.service.inter.AiTradeAssistant;
import com.huge.aiexchange.service.inter.AssistantInter;
import com.huge.aiexchange.tool.AiTradeTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssistantConfiguration {

    @Resource
    private ChatModel qwenChatModel;

    @Resource
    private AiTradeTool aiTradeTool;

    @Bean
    public AssistantInter getAssistant() {
        return AiServices.builder(AssistantInter.class)
                .chatModel(qwenChatModel)
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    @Bean
    public AiTradeAssistant getAiTradeAssistant() {
        return AiServices.builder(AiTradeAssistant.class)
                .chatModel(qwenChatModel)
                .tools(aiTradeTool)
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

}
