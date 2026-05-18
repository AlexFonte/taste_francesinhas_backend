package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.AuthDTO.AuthResponse;
import com.app.tastefrancesinhasbackend.dto.AuthDTO.ChangePasswordRequest;
import com.app.tastefrancesinhasbackend.dto.AuthDTO.LoginRequest;
import com.app.tastefrancesinhasbackend.dto.AuthDTO.RefreshRequest;
import com.app.tastefrancesinhasbackend.dto.AuthDTO.RegisterRequest;
import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.entity.enums.Role;
import com.app.tastefrancesinhasbackend.exception.ConflictException;
import com.app.tastefrancesinhasbackend.exception.UnauthorizedException;
import com.app.tastefrancesinhasbackend.repository.UserRepository;
import com.app.tastefrancesinhasbackend.security.CurrentUserContext;
import com.app.tastefrancesinhasbackend.security.JwtService;
import com.app.tastefrancesinhasbackend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CurrentUserContext currentUser;

    // Crea el usuario con rol USER, lo guarda y devuelve tokens listos para usar.
    // Así el cliente puede empezar sin tener que hacer, ya que se hara un "autologin".
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("El email ya está registrado");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        CustomUserDetails customUserDetails = CustomUserDetails.from(user);

        return new AuthResponse(
                jwtService.generateAccessToken(customUserDetails),
                jwtService.generateRefreshToken(customUserDetails),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );
    }

    // Valida el refresh token y emite un par nuevo. El viejo lo descarta el cliente.
    public AuthResponse refresh(RefreshRequest request) {
        // Extraemos el email del refresh token - si está expirado o es inválido, JJWT lanza excepción
        String email;
        try {
            email = jwtService.extractUsername(request.refreshToken());
        } catch (Exception e) {
            throw new UnauthorizedException("Refresh token inválido o expirado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        CustomUserDetails customUserDetails = CustomUserDetails.from(user);

        if (!jwtService.isValidRefreshToken(request.refreshToken(), customUserDetails)) {
            throw new UnauthorizedException("Refresh token inválido o expirado");
        }

        // Emitimos un nuevo par de tokens
        return new AuthResponse(
                jwtService.generateAccessToken(customUserDetails),
                jwtService.generateRefreshToken(customUserDetails),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );
    }

    // Spring Security verifica email + bcrypt. Si pasa, extraemos el customUserDetails del resultado.
    public AuthResponse login(LoginRequest request) {
        // Delega en Spring Security la verificación de email + password con bcrypt
        // Si falla lanza BadCredentialsException --> capturada por GlobalExceptionHandler --> 401
        Authentication authResult = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // El CustomUserDetails lo construyo UserDetailsServiceImpl durante la autenticacion a partir
        // del User cargado de BD.
        CustomUserDetails customUserDetails = (CustomUserDetails) authResult.getPrincipal();

        return new AuthResponse(
                jwtService.generateAccessToken(customUserDetails),
                jwtService.generateRefreshToken(customUserDetails),
                customUserDetails.name(),
                customUserDetails.email(),
                customUserDetails.role().name(),
                customUserDetails.id()
        );
    }

    // Cambio de contraseña: recupera el User actual via CurrentUserContext,
    // verifica la contraseña actual contra el bcrypt en BD y, si coincide, actualiza al hash nuevo.
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUser.getUser();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new UnauthorizedException("La contraseña actual no es correcta");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
