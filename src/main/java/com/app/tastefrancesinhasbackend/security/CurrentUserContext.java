package com.app.tastefrancesinhasbackend.security;

import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.exception.UnauthorizedException;
import com.app.tastefrancesinhasbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

// Bean request-scoped que da acceso al usuario autenticado desde cualquier capa sin tener
// que pasar Authentication por parametro. El proxyMode hace falta porque lo inyectamos
// en singletons (services); Spring resuelve el bean real al usarlo.
@Component
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class CurrentUserContext {

    private final UserRepository userRepository;
    private User userCached;

    // Devuelve el id del usuario actual leyendo el princpal de AutenticatedUser del SecurityContext.
    public Long id() {
        return getAutenticatedUser().id();
    }

    // Devuelve la entidad User completa. La primera llamada del request hace findById;
    // las siguientes en el mismo request devuelven la copia en "cache".
    public User getUser() {
        if (userCached == null) {
            userCached = userRepository.findById(id())
                    .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));
        }
        return userCached;
    }

    public String email() {
        return getAutenticatedUser().email();
    }

    private AuthenticatedUser getAutenticatedUser() {
        Object p = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (p instanceof AuthenticatedUser u) return u;
        throw new UnauthorizedException("No autenticado");
    }
}