package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.AuthDTO.AuthResponse;
import com.app.tastefrancesinhasbackend.dto.AuthDTO.ChangePasswordRequest;
import com.app.tastefrancesinhasbackend.dto.AuthDTO.LoginRequest;
import com.app.tastefrancesinhasbackend.dto.AuthDTO.RefreshRequest;
import com.app.tastefrancesinhasbackend.dto.AuthDTO.RegisterRequest;
import com.app.tastefrancesinhasbackend.dto.AuthDTO.UserStatsResponse;
import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.entity.enums.Role;
import com.app.tastefrancesinhasbackend.exception.ConflictException;
import com.app.tastefrancesinhasbackend.exception.UnauthorizedException;
import com.app.tastefrancesinhasbackend.repository.FrancesinhaRepository;
import com.app.tastefrancesinhasbackend.repository.ReviewRepository;
import com.app.tastefrancesinhasbackend.repository.UserRepository;
import com.app.tastefrancesinhasbackend.security.JwtService;
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
    private final ReviewRepository reviewRepository;
    private final FrancesinhaRepository francesinhaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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

        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
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

        if (!jwtService.isValidRefreshToken(request.refreshToken(), user)) {
            throw new UnauthorizedException("Refresh token inválido o expirado");
        }

        // Emitimos un nuevo par de tokens
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );
    }

    // Spring Security verifica email + bcrypt. Si pasa, extraemos el usuario del resultado.
    public AuthResponse login(LoginRequest request) {
        // Delega en Spring Security la verificación de email + password con bcrypt
        // Si falla lanza BadCredentialsException --> capturada por GlobalExceptionHandler --> 401
        Authentication authResult = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // El User ya fue cargado de BD por UserDetailsServiceImpl durante la autenticación
        User user = (User) authResult.getPrincipal();

        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );
    }

    // Cambio de contraseña, se recupera el usuario de base de datos,
    // se verifica que la contraseña orignal sea la misma uqe la de bd, si es igual se procedes a actualizar la contraseña
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new UnauthorizedException("La contraseña actual no es correcta");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    // ver todas las propuestas.
    public UserStatsResponse getStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        long reviewsCount   = reviewRepository.countByUserId(user.getId());
        long proposalsCount = francesinhaRepository.countByProposedById(user.getId());

        return new UserStatsResponse(reviewsCount, proposalsCount);
    }
}
