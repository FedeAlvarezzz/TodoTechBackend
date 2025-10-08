package co.todotech.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final Environment env;
    private Algorithm algorithm;

    @Value("${jwt.secret:default_jwt_secret_muy_largo_y_seguro_minimo_32_caracteres}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // Default 24 horas
    private Long expiration;

    @Value("${jwt.issuer:todotech-app}")
    private String issuer;

    private static final int MIN_SECRET_LENGTH = 32;
    private static final String DEFAULT_DEV_SECRET = "default_jwt_secret_muy_largo_y_seguro_minimo_32_caracteres";

    public JwtUtil(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        validateAndInitializeSecret();
        log.info("✅ JwtUtil initialized - Expiration: {}ms ({} horas)",
                expiration, expiration / 3600000);
    }

    private void validateAndInitializeSecret() {
        // Verificar si estamos usando el secret por defecto (desarrollo)
        boolean usingDefaultSecret = DEFAULT_DEV_SECRET.equals(jwtSecret);

        if (usingDefaultSecret) {
            log.warn("🚨 USING DEFAULT JWT SECRET - NOT RECOMMENDED FOR PRODUCTION");
            log.warn("💡 Set JWT_SECRET environment variable for production security");
        } else {
            log.info("🔐 Using custom JWT secret from configuration");
        }

        // Validar longitud del secret para seguridad
        if (jwtSecret.length() < MIN_SECRET_LENGTH) {
            log.error("❌ JWT secret too short: {} characters (minimum: {})",
                    jwtSecret.length(), MIN_SECRET_LENGTH);
            throw new IllegalStateException(
                    String.format("JWT secret must be at least %d characters long", MIN_SECRET_LENGTH)
            );
        }

        try {
            this.algorithm = Algorithm.HMAC256(jwtSecret);
            log.info("✅ JWT algorithm initialized successfully");
        } catch (Exception e) {
            log.error("❌ Failed to initialize JWT algorithm: {}", e.getMessage());
            throw new IllegalStateException("Error initializing JWT algorithm", e);
        }
    }

    public String generateToken(String username, Long userId, String role) {
        validateInputParameters(username, userId, role);

        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);

            String token = JWT.create()
                    .withIssuer(issuer)
                    .withSubject(username.trim())
                    .withClaim("userId", userId)
                    .withClaim("role", role.toUpperCase())
                    .withIssuedAt(now)
                    .withExpiresAt(expiryDate)
                    .withJWTId(generateJwtId())
                    .sign(algorithm);

            log.debug("✅ Token generated for user: {} (Role: {}, Expires: {})",
                    username, role, expiryDate);
            return token;

        } catch (JWTCreationException e) {
            log.error("❌ JWT creation failed for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Error generating authentication token", e);
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("⚠️ Attempt to validate null/empty token");
            return false;
        }

        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();

            DecodedJWT jwt = verifier.verify(token);

            // Validación adicional de expiración
            if (jwt.getExpiresAt().before(new Date())) {
                log.debug("⌛ Token expired for user: {}", jwt.getSubject());
                return false;
            }

            log.debug("✅ Token valid for user: {}", jwt.getSubject());
            return true;

        } catch (TokenExpiredException e) {
            log.debug("⌛ Token expired: {}", e.getMessage());
            return false;
        } catch (JWTVerificationException e) {
            log.warn("⚠️ Token verification failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Unexpected token validation error: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return safelyDecodeToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return safelyDecodeToken(token).getClaim("userId").asLong();
    }

    public String getRoleFromToken(String token) {
        return safelyDecodeToken(token).getClaim("role").asString();
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            return safelyDecodeToken(token).getExpiresAt();
        } catch (Exception e) {
            log.debug("Cannot get expiration from invalid token");
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public long getTimeUntilExpiration(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            if (expiration == null) return 0;

            long timeLeft = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, timeLeft);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getTokenInfo(String token) {
        try {
            DecodedJWT jwt = safelyDecodeToken(token);
            return String.format("User: %s, Role: %s, Expires: %s",
                    jwt.getSubject(), jwt.getClaim("role").asString(), jwt.getExpiresAt());
        } catch (Exception e) {
            return "Invalid token";
        }
    }

    private DecodedJWT safelyDecodeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new JWTDecodeException("Token cannot be null or empty");
        }

        try {
            DecodedJWT jwt = JWT.decode(token);

            // Validaciones básicas de estructura
            if (jwt.getSubject() == null || jwt.getSubject().trim().isEmpty()) {
                throw new JWTDecodeException("Token subject (username) is missing");
            }

            if (jwt.getExpiresAt() == null) {
                throw new JWTDecodeException("Token expiration is missing");
            }

            if (jwt.getClaim("userId").isNull()) {
                throw new JWTDecodeException("Token userId claim is missing");
            }

            if (jwt.getClaim("role").isNull()) {
                throw new JWTDecodeException("Token role claim is missing");
            }

            return jwt;

        } catch (JWTDecodeException e) {
            log.error("❌ Token decoding failed: {}", e.getMessage());
            throw new JWTDecodeException("Invalid token format", e);
        }
    }

    private void validateInputParameters(String username, Long userId, String role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
    }

    private String generateJwtId() {
        return String.format("jti_%d_%06x",
                System.currentTimeMillis(),
                (int) (Math.random() * 0xFFFFFF));
    }

    /**
     * Verificación avanzada con DecodedJWT completo
     */
    public DecodedJWT verifyAndGetDecodedToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.error("❌ Token verification failed: {}", e.getMessage());
            throw new JWTVerificationException("Token verification failed", e);
        }
    }
}