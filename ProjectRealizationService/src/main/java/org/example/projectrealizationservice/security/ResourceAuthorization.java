package org.example.projectrealizationservice.security;

import java.util.Objects;

public final class ResourceAuthorization {

    private ResourceAuthorization() {
    }

    public static Long requireCurrentUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("Unauthenticated user cannot perform this action.");
        }
        return userId;
    }

    public static void assertCurrentUserIsOwner(Long creatorId) {
        if (!Objects.equals(creatorId, SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("You are not allowed to modify this resource.");
        }
    }
}
