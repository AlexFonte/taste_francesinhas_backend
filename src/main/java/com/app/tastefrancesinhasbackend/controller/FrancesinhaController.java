package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaRequest;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaResponse;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaStatusRequest;
import com.app.tastefrancesinhasbackend.config.ApiConstants;
import com.app.tastefrancesinhasbackend.dto.PageResponse;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import com.app.tastefrancesinhasbackend.service.FrancesinhaService;
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
public class FrancesinhaController {

    private final FrancesinhaService francesinhaService;

    /**
     * GET /francesinhas  público, lista solo las aceptadas
     * Filtros opcionales  ?name=franc&city=oporto&type=CLASICA
     * Paginación: ?page=0&size=10&sort=avgScore,desc
     * @param name
     * @param city
     * @param type
     * @param pageable
     * @return
     */
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Map<String, Object>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) FrancesinhaType type,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(francesinhaService.findAllAccepted(name, city, type, pageable), ApiConstants.FRANCESINHAS_CONTENT_KEY));
    }


    /**
     * GET /francesinhas/{id} — público, detalle de una francesinha aceptada
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<FrancesinhaResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(francesinhaService.findById(id));
    }

    // GET /francesinhas/pending — solo ADMIN, lista las pendientes de revisión
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> findAllPending(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(francesinhaService.findAllPending(pageable), ApiConstants.FRANCESINHAS_CONTENT_KEY));
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