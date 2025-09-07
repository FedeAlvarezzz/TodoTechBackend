package co.todotech.configuration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class Auth0AuthoritiesConverter {
    private Auth0AuthoritiesConverter(){}

    public static JwtAuthenticationConverter jwtAuthConverter() {
        var conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(permissionsConverter());
        return conv;
    }

    private static Converter<Jwt, Collection<GrantedAuthority>> permissionsConverter() {
        return jwt -> {
            var permissions = (List<String>) jwt.getClaims().getOrDefault("permissions", List.of());
            return permissions.stream()
                    .map(SimpleGrantedAuthority::new) // p.ej. "admin:all", "users:read"
                    .collect(Collectors.toSet());
        };
    }
}
