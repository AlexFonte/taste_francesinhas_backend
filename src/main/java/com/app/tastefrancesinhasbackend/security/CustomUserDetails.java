package com.app.tastefrancesinhasbackend.security;

import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.entity.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Adaptador User (domain) -> UserDetails (security). Solo se usa en el flujo de login para
// que Spring Security verifique password y roles sin que la entidad conozca el framework.
// Tras el login, los requests autenticados via JWT trabajan con AuthenticatedUser (sin password).
public record CustomUserDetails(Long id, String name, String email, String password, Role role) implements UserDetails {

    public static CustomUserDetails from(User user) {
        return new CustomUserDetails(user.getId(), user.getName(), user.getEmail(), user.getPassword(), user.getRole());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}