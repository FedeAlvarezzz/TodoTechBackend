package co.todotech.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // habilita CORS y usa tu CorsConfig
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/public/**").permitAll()
                        .requestMatchers("/usuarios/login").permitAll()      //cambio
                        .requestMatchers("/api/admin/**").hasAuthority("admin:all")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(Auth0AuthoritiesConverter.jwtAuthConverter())
                                .decoder(Auth0JwtDecoder.decoder())
                        )
                );

        return http.build();
    }
}
