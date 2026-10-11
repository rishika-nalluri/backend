package com.pawconnect.security;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

/** The signed-in account, read from the authenticated principal set by SupabaseJwtAuthFilter. */
public record CurrentUser(UUID id, String role) {

    public static CurrentUser from(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal) || principal.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in required");
        }
        return new CurrentUser(principal.getId(), principal.getRole());
    }

    /** 403 unless the account belongs to the expected portal. */
    public CurrentUser requireRole(String expected) {
        if (!expected.equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This action is not available to this account type");
        }
        return this;
    }
}
