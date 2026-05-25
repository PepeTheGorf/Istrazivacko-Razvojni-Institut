package org.example.projectrealizationservice.security;

import org.example.projectrealizationservice.model.Role;

public record JwtUserPrincipal(Long id, String email, Role role) {
}
