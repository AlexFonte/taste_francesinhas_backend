package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaRequest;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaResponse;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaStatusRequest;
import com.app.tastefrancesinhasbackend.service.FrancesinhaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/francesinhas", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class FrancesinhaController {

    private final FrancesinhaService francesinhaService;

    // GET /francesinhas — público, lista solo las aceptadas
    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<FrancesinhaResponse>> findAll() {
        return ResponseEntity.ok(francesinhaService.findAllAccepted());
    }

    // GET /francesinhas/{id} — público, detalle de una francesinha aceptada
    @GetMapping("/{id}")
    public ResponseEntity<FrancesinhaResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(francesinhaService.findById(id));
    }

    // GET /francesinhas/pending — solo ADMIN, lista las pendientes de revisión
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FrancesinhaResponse>> findAllPending() {
        return ResponseEntity.ok(francesinhaService.findAllPending());
    }

    // GET /francesinhas/pending/{id} — solo ADMIN, detalle sin filtro de estado
    @GetMapping("/pending/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FrancesinhaResponse> findByIdForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(francesinhaService.findByIdForAdmin(id));
    }

    // POST /francesinhas/propose — solo USER, propone una nueva francesinha
    // Queda en estado PENDING hasta que un ADMIN la revise
    @PostMapping(value = "/propose", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FrancesinhaResponse> propose(@Valid @RequestBody FrancesinhaRequest request,
                                                       Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(francesinhaService.propose(request, auth));
    }

    // PATCH /francesinhas/pending/{id}/status — solo ADMIN, aprueba o rechaza
    @PatchMapping(value = "/pending/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FrancesinhaResponse> updateStatus(@PathVariable Long id,
                                                            @Valid @RequestBody FrancesinhaStatusRequest request) {
        return ResponseEntity.ok(francesinhaService.updateStatus(id, request));
    }
}