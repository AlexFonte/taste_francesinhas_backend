package com.app.tastefrancesinhasbackend.security;

import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Implementación de UserDetailsService separada de SecurityConfig para evitar
// el ciclo de dependencias: SecurityConfig --> JwtAuthenticationFilter --> UserDetailsService --> SecurityConfig
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring Security llama a este método durante el login (AuthenticationManager.authenticate).
    // Carga la entidad User de BD y la adapta a CustomUserDetails (la implementacion de UserDetails)
    // De este modo la entidad User no se expone.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
        return CustomUserDetails.from(user);
    }
}