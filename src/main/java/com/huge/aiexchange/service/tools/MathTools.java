package com.huge.aiexchange.service.tools;

import dev.langchain4j.agent.tool.Tool;

public class MathTools {

    @Tool
    public String add(String a, String b){
        return String.valueOf(Integer.parseInt(a) + Integer.parseInt(b));
    }


}
