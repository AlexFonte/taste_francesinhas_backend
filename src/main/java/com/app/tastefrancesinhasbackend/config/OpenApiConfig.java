package com.app.tastefrancesinhasbackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Taste Francesinhas API",
        version = "1.0",
        description = "API REST para la plataforma de valoración de francesinhas portuguesas",
        contact = @Contact(name = "Alex Ramos da Fonte Fonte", email = "aramosda@uoc.edu")
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "El access token se obtiene en /auth/login (sin el prefijo 'Bearer')"
)
public class OpenApiConfig {}
