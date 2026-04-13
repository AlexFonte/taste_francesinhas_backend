package com.app.tastefrancesinhasbackend.security;

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

    // Spring Security llama a este método automáticamente durante la autenticación
    // para cargar el usuario de BD a partir del username (en nuestro caso, el email)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}
