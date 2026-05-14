package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.config.ApiConstants;
import com.app.tastefrancesinhasbackend.dto.PageResponse;
import com.app.tastefrancesinhasbackend.dto.ProfileDTO.UserStatsResponse;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Datos del usuario autenticado: stats, reviews y propuestas")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Estadisticas del usuario", description = "Devuelve el numero de reviews y propuestas del usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Estadisticas obtenidas")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/stats")
    public ResponseEntity<UserStatsResponse> getStats(Authentication auth) {
        return ResponseEntity.ok(profileService.getStats(auth.getName()));
    }

    @Operation(summary = "Listar mis reviews", description = "Solo USER. Reviews del usuario con datos minimos de la francesinha valorada.")
    @ApiResponse(responseCode = "200", description = "Listado paginado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Los administradores no tienen reviews propias")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/reviews")
    public ResponseEntity<Map<String, Object>> getMyReviews(Authentication auth,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(
                profileService.getMyReviews(auth.getName(), pageable),
                ApiConstants.REVIEWS_CONTENT_KEY
        ));
    }

    @Operation(summary = "Listar mis propuestas", description = "Solo USER. Propuestas del usuario, todos los estados. Filtro opcional por status (PENDING, ACCEPTED, REJECTED).")
    @ApiResponse(responseCode = "200", description = "Listado paginado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Los administradores no proponen francesinhas")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/proposals")
    public ResponseEntity<Map<String, Object>> getMyProposals(Authentication auth,
            @RequestParam(required = false) FrancesinhaStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(
                profileService.getMyProposals(auth.getName(), status, pageable),
                ApiConstants.PROPOSALS_CONTENT_KEY
        ));
    }
}