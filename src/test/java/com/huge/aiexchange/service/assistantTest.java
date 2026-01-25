package com.huge.aiexchange.service;

import com.huge.aiexchange.service.inter.AssistantInter;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class assistantTest {

    @Resource
    private AssistantInter assistant;

    @Test
    void isAvailable() {
        final boolean available = assistant.isAvailable(0,"1+1=3");
        System.out.println(available);
    }


    @Test
    public void toolsTest(){
        final String result = assistant.getAnswer(0, "请问2154416516+244545等于多少");
        System.out.println(result);
    }
}