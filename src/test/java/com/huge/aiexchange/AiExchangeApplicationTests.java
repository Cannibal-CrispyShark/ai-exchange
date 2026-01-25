package com.huge.aiexchange;

import com.huge.aiexchange.web3.Exmaple;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiExchangeApplicationTests {

    @Resource
    private Exmaple exmaple;

    @Test
    void contextLoads() {

        exmaple.testAlphaVantage();

    }

}
