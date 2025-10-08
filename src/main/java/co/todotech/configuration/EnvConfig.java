package co.todotech.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class EnvConfig {

    private final ConfigurableEnvironment environment;

    public EnvConfig(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void loadEnv() {
        try {
            System.out.println("🔍 Loading .env file from: " + System.getProperty("user.dir"));

            Dotenv dotenv = Dotenv.configure()
                    .directory("./") // Busca en la raíz del proyecto
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> envMap = new HashMap<>();

            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                envMap.put(key, value);

                // También establecer como variable del sistema
                System.setProperty(key, value);

                System.out.println("✅ Loaded: " + key + " = " +
                        (isSensitiveKey(key) ? "***" : value));
            });

            // AGREGAR AL ENVIRONMENT DE SPRING - ESTO ES LO MÁS IMPORTANTE
            MapPropertySource dotenvPropertySource = new MapPropertySource("dotenv", envMap);
            environment.getPropertySources().addFirst(dotenvPropertySource);

            System.out.println("🎯 .env file loaded successfully with " + envMap.size() + " variables");

        } catch (Exception e) {
            System.err.println("❌ ERROR loading .env: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load .env file - Application cannot start without environment variables", e);
        }
    }

    private boolean isSensitiveKey(String key) {
        return key.toLowerCase().contains("password") ||
                key.toLowerCase().contains("secret") ||
                key.toLowerCase().contains("key") ||
                key.toLowerCase().contains("token") ||
                key.toLowerCase().contains("email");
    }
}