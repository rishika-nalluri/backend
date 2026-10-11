package com.pawconnect.ai;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * The only path from the backend to the AI service. Every call carries the internal token, because the AI
 * service refuses requests without it. Callers catch RuntimeException and fall back to rules.
 */
@Component
public class AiGatewayClient {

    private final RestClient http;

    public AiGatewayClient(
            RestClient.Builder builder,
            @Value("${pawconnect.ai-service.base-url}") String baseUrl,
            @Value("${pawconnect.ai-service.internal-token:}") String internalToken) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(20));
        this.http = builder
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader("X-Internal-Token", internalToken)
                .build();
    }

    public Map<String, Object> risk(Map<String, Object> payload) {
        return http.post()
                .uri("/risk")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public String chat(String userId, String message, Map<String, Object> dogContext) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("user_id", userId);
        body.put("message", message);
        if (dogContext != null) {
            body.put("dog_context", dogContext);
        }
        Map<String, Object> reply = http.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        Object text = reply == null ? null : reply.get("reply");
        return text instanceof String s ? s : null;
    }
}
