package com.app.tastefrancesinhasbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration,
            @Value("${app.jwt.refresh-expiration}") long refreshExpiration
    ) {
        // Convierte el secret en una clave HMAC-SHA256
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    // Genera un access token (15 min)
    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails, expiration, Map.of());
    }

    // Genera un refresh token (7 días) con claim extra para distinguirlo
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails, refreshExpiration, Map.of("type", "refresh"));
    }

    private String buildToken(UserDetails userDetails, long ttl, Map<String, Object> extraClaims) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())   // el email del usuario
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttl))
                .signWith(signingKey)
                .compact();
    }

    // Valida que el token es un access token, pertenece al usuario y no ha expirado
    // Rechaza explícitamente los refresh tokens para evitar que se usen como access tokens
    public boolean isValid(String token, UserDetails userDetails) {
        final String email = extractUsername(token);
        final boolean isRefresh = "refresh".equals(extractClaim(token, c -> c.get("type", String.class)));
        return email.equals(userDetails.getUsername()) && !isExpired(token) && !isRefresh;
    }

    // Valida que el token es un refresh token válido y pertenece al usuario
    public boolean isValidRefreshToken(String token, UserDetails userDetails) {
        final String email = extractUsername(token);
        final boolean isRefresh = "refresh".equals(extractClaim(token, c -> c.get("type", String.class)));
        return email.equals(userDetails.getUsername()) && !isExpired(token) && isRefresh;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Método genérico para extraer cualquier claim del payload del token
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
