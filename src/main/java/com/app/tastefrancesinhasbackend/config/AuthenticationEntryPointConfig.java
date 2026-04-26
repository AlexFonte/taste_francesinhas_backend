package com.app.tastefrancesinhasbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
public class AuthenticationEntryPointConfig {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Cuando llega una request anonima a un endpoint protegido (token ausente o caducado)
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
            pd.setTitle("Unauthorized");
            pd.setDetail("Token caducado o ausente");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            MAPPER.writeValue(response.getWriter(), pd);
        };
    }
}