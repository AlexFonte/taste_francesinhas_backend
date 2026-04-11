package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.dto.AuthDTO.AuthResponse;
import com.app.tastefrancesinhasbackend.dto.AuthDTO.LoginRequest;
import com.app.tastefrancesinhasbackend.dto.AuthDTO.RefreshRequest;
import com.app.tastefrancesinhasbackend.dto.AuthDTO.RegisterRequest;
import com.app.tastefrancesinhasbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Registro: crea el usuario con rol USER y devuelve los tokens directamente
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    // Login: verifica credenciales y devuelve access + refresh token
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Refresh: el cliente manda el refresh token (7 días) y recibe un nuevo par de tokens
    // Útil cuando el access token (15 min) ha expirado sin tener que volver a hacer login
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    // Logout: JWT es stateless, el servidor no puede invalidar tokens.
    // Se le dice al cliente que descarte el token. La responsabilidad es del frontend.
    // Si se necesita invalidación real en el futuro → implementar blacklist con Redis.
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }
}