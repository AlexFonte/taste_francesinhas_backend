package com.app.tastefrancesinhasbackend.security;

import com.app.tastefrancesinhasbackend.entity.enums.Role;

// Representa al usuario autenticado durante un request HTTP. Spring Security lo guarda en
// el SecurityContext (es el "principal" en jerga de Spring) y desde ahi cualquier capa puede
// consultarlo a traves de CurrentUserContext.
//
// El JwtAuthenticationFilter lo crea leyendo los claims del JWT (uid, sub=email, role) cuando
// llega la peticion, asi que NO hace falta query a BD para tenerlo.
//
// Solo contiene los 3 datos minimos para autorizar peticiones (id para queries, email para logs,
// role para checks de @PreAuthorize). NO incluye la password (no la necesitamos despues del login)
// ni el name (solo se usa en la pantalla de perfil y va aparte en la AuthResponse del login).
// Si algun service necesita la entidad User completa, la pide al CurrentUserContext.getUser().
public record AuthenticatedUser(Long id, String email, Role role) {}