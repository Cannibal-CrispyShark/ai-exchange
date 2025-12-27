package com.huge.aiexchange.aiservice;


import com.huge.aiexchange.aiservice.inter.AssistantInter;
import com.huge.aiexchange.aiservice.tools.MathTools;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssistantRepository {

    @Resource
    private ChatModel qwenChatModel;

    @Bean
    public AssistantInter getAssistant() {
        return AiServices.builder(AssistantInter.class)
                .chatModel(qwenChatModel)
                .tools(new MathTools())
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}
