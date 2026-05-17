package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.dto.ReviewDTO.ReviewRequest;
import com.app.tastefrancesinhasbackend.dto.ReviewDTO.ReviewResponse;
import com.app.tastefrancesinhasbackend.dto.ReviewsPageResponse;
import com.app.tastefrancesinhasbackend.exception.BadRequestException;
import com.app.tastefrancesinhasbackend.service.ReviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/francesinhas/{francesinhaId}/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Valoraciones de francesinhas")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Listar reviews de una francesinha", description = "Público. Solo francesinhas en estado ACCEPTED.")
    @ApiResponse(responseCode = "200", description = "Lista de reviews")
    @ApiResponse(responseCode = "404", description = "Francesinha no encontrada o no aprobada")
    @GetMapping
    public ResponseEntity<ReviewsPageResponse<ReviewResponse>> findAll(@PathVariable Long francesinhaId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ReviewsPageResponse.of(reviewService.findByFrancesinha(francesinhaId, pageable)));
    }

    @Operation(summary = "Publicar una review", description = "Solo USER. Un usuario puede publicar múltiples reviews sobre la misma francesinha.")
    @ApiResponse(responseCode = "201", description = "Review creada")
    @ApiResponse(responseCode = "400", description = "Datos invalidos o foto invalida (tipo no soportado)")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Los administradores no pueden hacer reviews")
    @ApiResponse(responseCode = "404", description = "Francesinha no encontrada o no aprobada")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = {"", "/"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewResponse> create(@PathVariable Long francesinhaId,
                                                 @RequestPart("review") ReviewRequest request,
                                                 @RequestPart(value = "file", required = false) MultipartFile image,
                                                 Authentication auth) {

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(francesinhaId, request, image, auth));
    }
}
