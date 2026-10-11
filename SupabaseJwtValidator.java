package com.pawconnect.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * Verifies Supabase access tokens. Newer Supabase projects sign tokens with asymmetric keys, which are
 * checked against the project's JWKS. Older projects use an HS256 secret, which is checked when configured.
 * Both paths require the issuer to be this project's auth endpoint.
 */
@Slf4j
@Component
public class SupabaseJwtValidator {

    private final SecretKey legacyKey;
    private final JwtDecoder asymmetricDecoder;

    public SupabaseJwtValidator(@Value("${supabase.jwt.secret:}") String legacySecret,
                                @Value("${supabase.url:}") String supabaseUrl) {
        String secret = legacySecret == null ? "" : legacySecret.trim();
        this.legacyKey = secret.length() >= 32
                ? Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))
                : null;
        if (!secret.isEmpty() && legacyKey == null) {
            log.warn("SUPABASE_JWT_SECRET is shorter than 32 characters and will be ignored");
        }

        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            this.asymmetricDecoder = null;
            log.warn("SUPABASE_URL is not set: only legacy HS256 tokens can be verified");
        } else {
            String issuer = supabaseUrl.replaceAll("/+$", "") + "/auth/v1";
            NimbusJwtDecoder decoder = NimbusJwtDecoder
                    .withJwkSetUri(issuer + "/.well-known/jwks.json")
                    .build();
            decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
            this.asymmetricDecoder = decoder;
        }
    }

    /** Verified claims, or null when no configured key accepts the token. */
    public Claims validateAndExtractClaims(String token) {
        if (legacyKey != null) {
            try {
                return Jwts.parser().verifyWith(legacyKey).build().parseSignedClaims(token).getPayload();
            } catch (RuntimeException notLegacy) {
                // Not signed with our HS256 secret. Try the asymmetric keys next.
            }
        }
        if (asymmetricDecoder != null) {
            try {
                org.springframework.security.oauth2.jwt.Jwt jwt = asymmetricDecoder.decode(token);
                return Jwts.claims().add(jwt.getClaims()).build();
            } catch (RuntimeException rejected) {
                log.debug("Token rejected: {}", rejected.getMessage());
            }
        }
        return null;
    }

    public UUID extractUserId(Claims claims) {
        if (claims == null || claims.getSubject() == null) {
            return null;
        }
        try {
            return UUID.fromString(claims.getSubject());
        } catch (IllegalArgumentException notAUuid) {
            return null;
        }
    }

    public String extractEmail(Claims claims) {
        return claims == null ? null : claims.get("email", String.class);
    }
}
