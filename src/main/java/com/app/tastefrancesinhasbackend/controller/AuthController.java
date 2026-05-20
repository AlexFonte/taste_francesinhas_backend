package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.dto.AuthDTO.*;
import com.app.tastefrancesinhasbackend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registro de usuario, login y renovación de tokens")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registro de nuevo usuario", description = "Crea una cuenta con rol USER y devuelve el access y refresh token para hacer un login automatico desde el front")
    @ApiResponse(responseCode = "201", description = "Cuenta creada,  devuelve accessToken y refreshToken")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (email mal formado, contraseña débil...)")
    @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Login", description = "Verifica credenciales y devuelve un el access y refresh token (access 1h + refresh 7d)")
    @ApiResponse(responseCode = "200", description = "Login correcto")
    @ApiResponse(responseCode = "400", description = "Body inválido")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Renovar tokens", description = "Intercambia el refresh token por un nuevo par de tokens")
    @ApiResponse(responseCode = "200", description = "Tokens renovados")
    @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(summary = "Cambiar contraseña", description = "Cambia la contraseña del usuario autenticado tras verificar la actual")
    @ApiResponse(responseCode = "204", description = "Contraseña actualizada")
    @ApiResponse(responseCode = "400", description = "Nueva contraseña no cumple los requisitos")
    @ApiResponse(responseCode = "401", description = "No autenticado o contraseña actual incorrecta")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping(value = "/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
