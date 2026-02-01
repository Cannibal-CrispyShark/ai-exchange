package com.huge.aiexchange.configuration;


import com.huge.aiexchange.service.inter.AiTradeAssistant;
import com.huge.aiexchange.service.inter.AssistantInter;
import com.huge.aiexchange.tool.AiTradeTool;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.qianfan.QianfanChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AssistantConfiguration {

    @Resource
    private ChatModel qwenChatModel;

    @Resource
    private AiTradeTool aiTradeTool;

    // 从配置文件读取DashScope API Key
    @Value("${langchain4j.community.dashscope.chat-model.api-key}")
    private String dashscopeApiKey;

    // 从配置文件读取千帆API Key和Secret Key
    @Value("${langchain4j.community.qianfan.chat-model.api-key}")
    private String qianfanApiKey;

    @Value("${langchain4j.community.qianfan.chat-model.secret-key}")
    private String qianfanSecretKey;

    @Bean(name = "qwenMaxAssistant")
    @Primary
    public AiTradeAssistant getQwenAiTradeAssistant() {
        return AiServices.builder(AiTradeAssistant.class)
                .chatModel(QwenChatModel.builder()
                        .apiKey(dashscopeApiKey)
                        .modelName("qwen-max")
                        .temperature(0.7f)
                        .build())
                .tools(aiTradeTool)
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    @Bean(name = "qianfanAssistant")
    public AiTradeAssistant getQianfanAiTradeAssistant() {
        return AiServices.builder(AiTradeAssistant.class)
                .chatModel(QianfanChatModel.builder()
                        .apiKey(qianfanApiKey)
                        .secretKey(qianfanSecretKey)
                        .endpoint("ep_yir5kvx9s_123")
                        .modelName("ERNIE-5.0-Thinking-Preview")
                        .temperature(0.7d)
                        .build())
                .tools(aiTradeTool)
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }


    @Bean(name = "CustomChatAssistant")
    public AssistantInter getAssistant() {
        return AiServices.builder(AssistantInter.class)
                .chatModel(qwenChatModel)
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }


}
