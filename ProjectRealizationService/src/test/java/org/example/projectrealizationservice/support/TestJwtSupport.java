package org.example.projectrealizationservice.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.projectrealizationservice.model.Role;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import javax.crypto.SecretKey;
import java.util.Date;

public final class TestJwtSupport {

    public static final String TEST_JWT_SECRET =
            "c3Rha2Vob2xkZXJzZXJ2aWNland0c2VjcmV0a2V5Zm9ydGgyNTZiaXRzMTIzNDU2Nzg5MA==";

    private TestJwtSupport() {
    }

    public static String token(Long userId, Role role) {
        return token(userId, "test@example.com", role);
    }

    public static String token(Long userId, String email, Role role) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_JWT_SECRET));
        return Jwts.builder()
                .subject(email)
                .claim("id", userId)
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
    }

    public static RequestPostProcessor bearer(Role role) {
        return bearer(1L, role);
    }

    public static RequestPostProcessor bearer(Long userId, Role role) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token(userId, role));
            return request;
        };
    }
}
