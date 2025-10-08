package co.todotech.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();
    private static final long TOKEN_EXPIRATION_HOURS = 24; // Limpiar tokens después de 24h

    public void blacklistToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            blacklistedTokens.put(token, LocalDateTime.now());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        return blacklistedTokens.containsKey(token);
    }

    @Scheduled(fixedRate = 3600000) // Ejecutar cada hora
    public void cleanExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(TOKEN_EXPIRATION_HOURS);

        blacklistedTokens.entrySet().removeIf(entry ->
                entry.getValue().isBefore(cutoff)
        );

        System.out.println("Limpieza de tokens blacklist completada. Tokens activos: " + blacklistedTokens.size());
    }
}