package com.aiqa.system.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AppConfig {

    @Bean
    public WebClient qwenWebClient(AiProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.getQwen().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getQwen().getApiKey())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    @Bean
    public Duration qwenTimeout(AiProperties properties) {
        return Duration.ofSeconds(properties.getQwen().getTimeoutSeconds());
    }
}
