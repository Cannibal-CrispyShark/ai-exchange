package com.huge.aiexchange;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.huge.aiexchange.mapper")
public class AiExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiExchangeApplication.class, args);
    }

}
