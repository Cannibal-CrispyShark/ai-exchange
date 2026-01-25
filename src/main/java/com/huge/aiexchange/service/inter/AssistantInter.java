package com.huge.aiexchange.service.inter;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AssistantInter {

    @SystemMessage("answer me in true or false")
    public Boolean isAvailable(@MemoryId Integer id ,@UserMessage String userMessage);

    @SystemMessage("use the method of add to resolve the problem")
    public String getAnswer(@MemoryId Integer id ,@UserMessage String userMessage);

}
