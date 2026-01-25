package com.huge.aiexchange.configuration;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlphavantageConfig {

    @Value("${alphavantage.api.key}")
    public static final String API_KEY = "YOUR_API_KEY";

    @Bean
    public AlphaVantage getAlphaVantage() {
        Config cfg = Config.builder()
                .key(API_KEY)
                .timeOut(10)
                .build();
        AlphaVantage.api().init(cfg);
        return AlphaVantage.api();
    }

}
