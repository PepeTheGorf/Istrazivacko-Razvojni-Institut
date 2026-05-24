package org.example.projectrealizationservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static JwtUserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserPrincipal principal) {
            return principal;
        }
        return null;
    }

    public static Long getCurrentUserId() {
        JwtUserPrincipal user = getCurrentUser();
        return user != null ? user.id() : null;
    }
}
