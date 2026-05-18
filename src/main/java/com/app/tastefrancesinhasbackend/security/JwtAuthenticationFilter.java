package com.app.tastefrancesinhasbackend.security;

import com.app.tastefrancesinhasbackend.entity.enums.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si no hay cabecera Authorization o no empieza por "Bearer ", dejamos pasar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraemos el token quitando el prefijo "Bearer " (exactamente 7 caracteres)
        final String token = authHeader.substring(7);

        try {
            // Parseamos el token UNA vez: valida firma y caducidad. Si falla salta excepcion y
            // dejamos pasar el request sin autenticar (Spring devolvera 401 si el endpoint lo requiere).
            Claims claims = jwtService.parseAndValidate(token);

            // Rechazamos los refresh tokens explicitamente: solo los access valen como autenticacion.
            if ("refresh".equals(claims.get("type", String.class))) {
                filterChain.doFilter(request, response);
                return;
            }

            // Evita re-autenticar si ya hay sesion activa en esta request.
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Long   uid   = claims.get("uid", Long.class);
                String email = claims.getSubject();
                Role   role  = Role.valueOf(claims.get("role", String.class));

                // Construimos el AutneitcaetedIUser sin tocar BD. Los services que necesiten la
                AuthenticatedUser authenticatedUser = new AuthenticatedUser(uid, email, role);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                authenticatedUser,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception ignored) {
            // token invalido, expirado o sin los claims esperados - Spring devolvera 401 si el endpoint lo requiere
        }

        filterChain.doFilter(request, response);
    }
}