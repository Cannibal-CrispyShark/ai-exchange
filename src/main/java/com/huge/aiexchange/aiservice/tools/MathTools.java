package com.huge.aiexchange.aiservice.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;

public class MathTools {

    @Tool
    public String add(String a, String b){
        return String.valueOf(Integer.parseInt(a) + Integer.parseInt(b));
    }


}
