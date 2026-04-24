package com.aiqa.system.service;

import com.aiqa.system.config.AiProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class QwenChatService {

    private final WebClient webClient;
    private final AiProperties properties;
    private final Duration timeout;

    public QwenChatService(WebClient qwenWebClient, AiProperties properties, Duration qwenTimeout) {
        this.webClient = qwenWebClient;
        this.properties = properties;
        this.timeout = qwenTimeout;
    }

    public String answer(String systemPrompt, String userPrompt) {
        if (properties.getQwen().isMockEnabled()) {
            return "Mock mode is enabled. The backend pipeline already covers agent routing, query rewrite, hybrid retrieval and evidence assembly.";
        }

        Map<String, Object> request = Map.of(
                "model", properties.getQwen().getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.2
        );

        Map<?, ?> response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(timeout)
                .block();

        if (response == null || response.get("choices") == null) {
            return "Model returned no valid result.";
        }
        List<?> choices = (List<?>) response.get("choices");
        if (choices.isEmpty()) {
            return "Model returned no candidate result.";
        }
        Map<?, ?> choice = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = (Map<?, ?>) choice.get("message");
        return String.valueOf(message.get("content"));
    }
}
