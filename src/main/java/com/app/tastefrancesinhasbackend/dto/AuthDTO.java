package com.app.tastefrancesinhasbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthDTO{

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                    message = "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número"
            ) String password
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String email,
            String role
    ) {}

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {}
}
