package co.todotech.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

//@Component  // ← COMENTA ESTA ANOTACIÓN
public class EnvChecker implements CommandLineRunner {

    private final Environment environment;

    public EnvChecker(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {
        /* COMENTA TODO EL CÓDIGO O ELIMÍNALO
        System.out.println("\n🔍 CHECKING ENVIRONMENT VARIABLES:");
        System.out.println("==================================");

        checkVariable("DB_USERNAME");
        checkVariable("DB_PASSWORD");
        checkVariable("JWT_SECRET");
        checkVariable("JWT_EXPIRATION");
        checkVariable("ADMIN_USERNAME");
        checkVariable("ADMIN_PASSWORD");
        checkVariable("EMAIL_USERNAME");
        checkVariable("EMAIL_PASSWORD");

        System.out.println("==================================\n");
        */
    }

    private void checkVariable(String key) {
        // COMENTA EL CÓDIGO
        /*
        String value = environment.getProperty(key);
        boolean isLoaded = value != null && !value.trim().isEmpty();

        System.out.println(key + ": " + (isLoaded ? "✅ LOADED" : "❌ MISSING"));

        if (isLoaded && !isSensitive(key)) {
            System.out.println("   Value: " + value);
        }
        */
    }

    private boolean isSensitive(String key) {
        return key.toLowerCase().contains("password") ||
                key.toLowerCase().contains("secret");
    }
}