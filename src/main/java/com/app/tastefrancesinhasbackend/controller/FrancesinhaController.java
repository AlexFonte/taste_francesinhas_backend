package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaRequest;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaResponse;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaStatusRequest;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.StatsResponse;
import com.app.tastefrancesinhasbackend.service.ReviewService;
import com.app.tastefrancesinhasbackend.config.ApiConstants;
import com.app.tastefrancesinhasbackend.dto.PageResponse;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import com.app.tastefrancesinhasbackend.service.FrancesinhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/francesinhas", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Francesinhas", description = "Listado, detalle, propuesta y moderación de francesinhas para el admin")
public class FrancesinhaController {

    private final FrancesinhaService francesinhaService;
    private final ReviewService reviewService;

    @Operation(summary = "Listar francesinhas aceptadas", description = "Filtros opcionales: name, city, type.")
    @ApiResponse(responseCode = "200", description = "Listado paginado")
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Map<String, Object>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) FrancesinhaType type,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(francesinhaService.findAllAccepted(name, city, type, pageable), ApiConstants.FRANCESINHAS_CONTENT_KEY));
    }

    @Operation(summary = "Detalle de una francesinha", description = "Público. 404 si no existe o no está aprobada.")
    @ApiResponse(responseCode = "200", description = "Francesinha encontrada")
    @ApiResponse(responseCode = "404", description = "No encontrada o no aprobada")
    @GetMapping("/{id}")
    public ResponseEntity<FrancesinhaResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(francesinhaService.findById(id));
    }

    @Operation(summary = "Estadísticas de moderación", description = "Solo ADMIN. Devuelve los contadores de francesinhas pendientes, aprobadas, rechazadas y el total.")
    @ApiResponse(responseCode = "200", description = "Contadores agregados")
    @ApiResponse(responseCode = "403", description = "Sin permiso de administrador")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(francesinhaService.getStats());
    }

    @Operation(summary = "Listar francesinhas pendientes", description = "Solo ADMIN. Lista las pendientes de revisión, ordenadas por fecha de creación ASC.")
    @ApiResponse(responseCode = "200", description = "Listado paginado")
    @ApiResponse(responseCode = "403", description = "Sin permiso de administrador")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> findAllPending(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(francesinhaService.findAllPending(pageable), ApiConstants.FRANCESINHAS_CONTENT_KEY));
    }

    @Operation(summary = "Detalle de francesinha pendiente", description = "Solo ADMIN. Muestra detalle de francesinha con estado pendiente.")
    @ApiResponse(responseCode = "200", description = "Francesinha encontrada")
    @ApiResponse(responseCode = "403", description = "Sin permiso de administrador")
    @ApiResponse(responseCode = "404", description = "No encontrada")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/pending/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FrancesinhaResponse> findByIdForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(francesinhaService.findByIdForAdmin(id));
    }

    @Operation(summary = "Proponer nueva francesinha", description = "Solo USER. La francesinha queda en estado PENDING hasta que un ADMIN la revise.")
    @ApiResponse(responseCode = "201", description = "Francesinha creada en estado PENDING")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Solo usuarios con rol USER pueden proponer")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/propose", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FrancesinhaResponse> propose(@Valid @RequestBody FrancesinhaRequest request,
                                                       Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(francesinhaService.propose(request, auth));
    }

    @Operation(summary = "Listar reviews de una francesinha pendiente", description = "Solo ADMIN. Devuelve las reviews sin filtrar por estado de la francesinha (la review que dejó el proponente cuando creó la propuesta).")
    @ApiResponse(responseCode = "200", description = "Listado paginado")
    @ApiResponse(responseCode = "403", description = "Sin permiso de administrador")
    @ApiResponse(responseCode = "404", description = "Francesinha no encontrada")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/pending/{id}/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> findReviewsForAdmin(@PathVariable Long id,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(reviewService.findByFrancesinhaForAdmin(id, pageable), ApiConstants.REVIEWS_CONTENT_KEY));
    }

    @Operation(summary = "Aprobar o rechazar francesinha", description = "Solo ADMIN. Cambia el estado a ACCEPTED o REJECTED.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Estado inválido")
    @ApiResponse(responseCode = "403", description = "Sin permiso de administrador")
    @ApiResponse(responseCode = "404", description = "Francesinha no encontrada")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(value = "/pending/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FrancesinhaResponse> updateStatus(@PathVariable Long id,
                                                            @Valid @RequestBody FrancesinhaStatusRequest request) {
        return ResponseEntity.ok(francesinhaService.updateStatus(id, request));
    }
}
