package com.app.tastefrancesinhasbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthDTO{

    public final static String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
    public final static String PASSWORD_ERROR_MSG = "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número";

    public record RegisterRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank @Pattern(
                    regexp = PASSWORD_PATTERN,
                    message = PASSWORD_ERROR_MSG
            ) String password
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String name,
            String email,
            String role,
            Long userId
    ) {}

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {}

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Pattern(
                    regexp = PASSWORD_PATTERN,
                    message = PASSWORD_ERROR_MSG
            ) String newPassword
    ) {}

    public record UserStatsResponse(
            long reviewsCount,
            long proposalsCount
    ) {}
}
