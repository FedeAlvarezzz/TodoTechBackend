package co.todotech.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.Date;

@Component
public class JwtUtil {

    private final Environment env; // ← MANTENER Environment

    @Value("${jwt.secret}")
    private String configuredSecret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private String secret;

    // Constructor con Environment
    public JwtUtil(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        this.secret = configuredSecret;

        if (secret == null || secret.trim().isEmpty()) {
            // Fallback para desarrollo
            if ("dev".equals(env.getProperty("spring.profiles.active"))) {
                secret = "dev-secret-only-for-development-not-production-2024";
                System.out.println("⚠️  USING DEV JWT SECRET - NOT FOR PRODUCTION ⚠️");
            } else {
                throw new IllegalStateException("JWT secret not configured. Set JWT_SECRET environment variable.");
            }
        }
    }

    public String generateToken(String username, Long userId, String role) {
        return JWT.create()
                .withSubject(username)
                .withClaim("userId", userId)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("userId").asLong();
    }

    public String getRoleFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("role").asString();
    }
}