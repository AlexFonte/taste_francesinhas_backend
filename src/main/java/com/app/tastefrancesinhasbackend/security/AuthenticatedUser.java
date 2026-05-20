package com.app.tastefrancesinhasbackend.security;

import com.app.tastefrancesinhasbackend.entity.enums.Role;

// Usuario autenticado del request actual. Spring Security lo guarda como "principal" en el
// SecurityContext; cualquier capa lo consulta via CurrentUserContext. Lo crea el
// JwtAuthenticationFilter con los claims del JWT (uid, sub=email, role) -> sin query a BD.
//
// Solo los 3 datos minimos para autorizar (id, email, role). NO lleva password ni name.
// Si un service necesita la entidad User completa -> CurrentUserContext.getUser().
public record AuthenticatedUser(Long id, String email, Role role) {
}