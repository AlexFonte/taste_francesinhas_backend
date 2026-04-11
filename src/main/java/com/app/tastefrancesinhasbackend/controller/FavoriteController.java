package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.dto.FavoriteDTO.FavoriteResponse;
import com.app.tastefrancesinhasbackend.dto.FavoriteDTO.ToggleResponse;
import com.app.tastefrancesinhasbackend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // GET /favorites — solo usuarios con rol USER (no admins)
    @GetMapping(value = {"", "/"})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<FavoriteResponse>> findAll(Authentication auth) {
        return ResponseEntity.ok(favoriteService.findByUser(auth));
    }

    // POST /favorites/{francesinhaId} — solo usuarios con rol USER (no admins)
    // Toggle: añade si no existe, elimina si ya existe
    @PostMapping("/{francesinhaId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ToggleResponse> toggle(@PathVariable Long francesinhaId, Authentication auth) {
        return ResponseEntity.ok(favoriteService.toggle(francesinhaId, auth));
    }
}