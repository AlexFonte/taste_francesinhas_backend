package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.dto.FavoriteDTO.ToggleResponse;
import com.app.tastefrancesinhasbackend.dto.FavoritesPageResponse;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import com.app.tastefrancesinhasbackend.service.FavoriteService;
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Gestión de francesinhas favoritas del usuario")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "Listar favoritos del usuario", description = "Solo USER. Filtros opcionales: name, city, type. Paginado.")
    @ApiResponse(responseCode = "200", description = "Listado paginado de favoritos")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Solo usuarios con rol USER")
    @GetMapping(value = {"", "/"})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FavoritesPageResponse> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) FrancesinhaType type,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth) {
        return ResponseEntity.ok(FavoritesPageResponse.of(favoriteService.findByUser(name, city, type, pageable, auth)));
    }

    @Operation(summary = "Comprobar si una francesinha esta como favorita", description = "Solo USER.")
    @GetMapping("/{francesinhaId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Boolean>> isFavorite(@PathVariable Long francesinhaId, Authentication auth) {
        return ResponseEntity.ok(Map.of("isFavorite", favoriteService.isFavorite(francesinhaId, auth)));
    }

    @Operation(summary = "Toggle favorito", description = "Solo USER. Añade la francesinha a favoritos si no estaba; la elimina si ya estaba.")
    @ApiResponse(responseCode = "200", description = "Estado del favorito tras la operación (added / removed)")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Solo usuarios con rol USER")
    @ApiResponse(responseCode = "404", description = "Francesinha no encontrada o no aprobada")
    @PostMapping("/{francesinhaId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ToggleResponse> toggle(@PathVariable Long francesinhaId, Authentication auth) {
        return ResponseEntity.ok(favoriteService.toggle(francesinhaId, auth));
    }
}
