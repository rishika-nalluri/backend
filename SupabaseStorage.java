package com.pawconnect.storage;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Issues short-lived links to private verification documents in Supabase Storage.
 * Uses the service role key, so it stays on the server and never reaches the browser.
 */
@Component
public class SupabaseStorage {

    public static final String BUCKET = "verification-docs";

    private final RestClient http;
    private final String baseUrl;

    public SupabaseStorage(
            RestClient.Builder builder,
            @Value("${supabase.url}") String baseUrl,
            @Value("${supabase.service-role-key:}") String serviceRoleKey) {
        this.baseUrl = baseUrl;
        this.http = builder
                .baseUrl(baseUrl)
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .build();
    }

    public String signedUrl(String path) {
        if (path.contains("..") || path.startsWith("/")) {
            throw new IllegalArgumentException("Invalid storage path");
        }
        Map<String, Object> response = http.post()
                .uri("/storage/v1/object/sign/" + BUCKET + "/" + path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("expiresIn", 600))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        Object signed = response == null ? null : response.get("signedURL");
        if (!(signed instanceof String signedPath)) {
            throw new IllegalStateException("Storage did not return a signed URL");
        }
        return baseUrl + "/storage/v1" + signedPath;
    }
}
