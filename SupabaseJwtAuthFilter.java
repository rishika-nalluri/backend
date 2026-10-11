package com.pawconnect.security;

import com.pawconnect.profile.ProfileProvisioningService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates requests that carry a valid Supabase token. The role comes only from the application's
 * profile row. It is never taken from the token, because users can edit their own token metadata.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SupabaseJwtAuthFilter extends OncePerRequestFilter {

    private final SupabaseJwtValidator jwtValidator;
    private final ProfileProvisioningService provisioning;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = extractJwtFromRequest(request);
            if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = jwtValidator.validateAndExtractClaims(token);
                UUID userId = claims == null ? null : jwtValidator.extractUserId(claims);
                if (userId != null) {
                    Optional<UserPrincipal> principal = provisioning.principalFor(
                            userId, jwtValidator.extractEmail(claims), userMetadata(claims));
                    principal.ifPresent(p -> {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                }
            }
        } catch (RuntimeException e) {
            log.error("Could not establish authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> userMetadata(Claims claims) {
        Object meta = claims.get("user_metadata");
        return meta instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
