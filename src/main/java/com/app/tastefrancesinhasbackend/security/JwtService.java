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

    // Genera un access token (1 hora)
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
        Claims claims = extractAllClaims(token);
        String email = claims.getSubject();
        boolean isRefresh = "refresh".equals(claims.get("type", String.class));
        boolean isExpired = claims.getExpiration().before(new Date());
        return email.equals(userDetails.getUsername()) && !isExpired && !isRefresh;
    }

    // Valida que el token es un refresh token válido y pertenece al usuario
    public boolean isValidRefreshToken(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        String email = claims.getSubject();
        boolean isRefresh = "refresh".equals(claims.get("type", String.class));
        boolean isExpired = claims.getExpiration().before(new Date());
        return email.equals(userDetails.getUsername()) && !isExpired && isRefresh;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Parsea el token una sola vez y devuelve todos los claims — usado en isValid e isValidRefreshToken
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Método genérico para extraer un claim puntual del payload del token
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }
}
