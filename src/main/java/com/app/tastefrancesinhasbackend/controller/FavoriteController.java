package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.config.ApiConstants;
import com.app.tastefrancesinhasbackend.dto.FavoriteDTO.ToggleResponse;
import com.app.tastefrancesinhasbackend.dto.PageResponse;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import com.app.tastefrancesinhasbackend.service.FavoriteService;
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
public class FavoriteController {

    private final FavoriteService favoriteService;

    // GET /favorites — solo usuarios con rol USER (no admins)
    // Filtros opcionales: ?name=franc&city=oporto&type=CLASICA
    // Paginación: ?page=0&size=10&sort=createdAt,desc
    @GetMapping(value = {"", "/"})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) FrancesinhaType type,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth) {
        return ResponseEntity.ok(PageResponse.of(favoriteService.findByUser(name, city, type, pageable, auth), ApiConstants.FAVORITES_CONTENT_KEY));
    }

    // POST /favorites/{francesinhaId} — solo usuarios con rol USER (no admins)
    // Toggle: añade si no existe, elimina si ya existe
    @PostMapping("/{francesinhaId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ToggleResponse> toggle(@PathVariable Long francesinhaId, Authentication auth) {
        return ResponseEntity.ok(favoriteService.toggle(francesinhaId, auth));
    }
}
