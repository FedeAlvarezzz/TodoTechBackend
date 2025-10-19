package co.todotech.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // ✅ PERMITIR ENDPOINTS PÚBLICOS SIN VALIDACIÓN DE TOKEN
        if (isPublicUrl(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                // Verificar si el token está en la blacklist PRIMERO
                if (tokenBlacklistService.isTokenBlacklisted(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": true, \"mensaje\": \"Token invalidado - Sesión cerrada\"}");
                    return;
                }

                // Luego validar el token JWT
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": true, \"mensaje\": \"Token inválido o expirado\"}");
                    return;
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": true, \"mensaje\": \"Error procesando el token: " + e.getMessage() + "\"}");
                return;
            }
        } else {
            // Para endpoints que requieren autenticación
            if (requiresAuthentication(requestURI)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": true, \"mensaje\": \"Token de autorización requerido\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // ✅ NUEVO MÉTODO: VERIFICAR SI ES URL PÚBLICA
    private boolean isPublicUrl(String requestURI) {
        return requestURI.equals("/usuarios/login") ||
                requestURI.equals("/usuarios/recordar-contrasena") ||
                requestURI.startsWith("/productos/publicos/"); // ✅ AGREGAR ENDPOINTS PÚBLICOS DE PRODUCTOS
    }

    // ✅ ACTUALIZAR MÉTODO EXISTENTE
    private boolean requiresAuthentication(String requestURI) {
        return !isPublicUrl(requestURI); // ✅ AHORA USA EL MISMO MÉTODO
    }
}