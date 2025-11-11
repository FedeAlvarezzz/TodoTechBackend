package co.todotech.configuration;

import co.todotech.security.JwtAuthenticationFilter;
import co.todotech.security.JwtUtil;
import co.todotech.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 🔓 ENDPOINTS PÚBLICOS (sin autenticación)
                        .requestMatchers(
                                "/usuarios/login",
                                "/usuarios/recordar-contrasena",
                                "/productos/publicos/**",
                                "/stripe/**",
                                "/paypal/**",
                                "/health",
                                "/"
                        ).permitAll()

                        // 🔐 ENDPOINTS QUE REQUIEREN AUTENTICACIÓN BÁSICA
                        .requestMatchers("/usuarios/logout").authenticated()

                        // 👑 ENDPOINTS ADMINISTRATIVOS (solo ADMIN)
                        .requestMatchers("/usuarios", "/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 🔒 TODAS LAS DEMÁS REQUESTS REQUIEREN AUTENTICACIÓN
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, tokenBlacklistService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ ORÍGENES PERMITIDOS - AGREGADA LA URL DE CLOUDFRONT
        configuration.setAllowedOrigins(Arrays.asList(
                "https://todotechshopfrontend.web.app",  // Firebase
                "https://d2jctboz5xbevf.cloudfront.net", // ← NUEVO: CloudFront
                "http://localhost:4200",                 // Desarrollo local
                "https://localhost:4200",                // Desarrollo local HTTPS
                "http://todotech-frontend.s3-website.us-east-2.amazonaws.com"
        ));

        // ✅ MÉTODOS HTTP PERMITIDOS
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));

        // ✅ HEADERS PERMITIDOS
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Auth-Token",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // ✅ HEADERS EXPUESTOS
        configuration.setExposedHeaders(Arrays.asList(
                "X-Auth-Token",
                "Authorization",
                "Content-Disposition"
        ));

        // ✅ PERMITIR CREDENCIALES
        configuration.setAllowCredentials(true);

        // ✅ TIEMPO DE VIDA DEL PRE-FLIGHT (OPTIONS)
        configuration.setMaxAge(3600L); // 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}