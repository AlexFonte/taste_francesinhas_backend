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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Crea una cuenta nueva con rol USER y devuelve los tokens para no tener que hacer login justo después.
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    // Verifica email y contraseña y devuelve un par de tokens (access 1h + refresh 7d).
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // El cliente manda el refresh token y recibe un nuevo par. Evita tener que hacer login cada hora.
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    // JWT es stateless: el servidor no puede invalidar tokens, así que le decimos al cliente que los descarte.
    // Si en el futuro se necesita invalidación real, habría que añadir una blacklist con Redis.
  /*  @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }*/
}
