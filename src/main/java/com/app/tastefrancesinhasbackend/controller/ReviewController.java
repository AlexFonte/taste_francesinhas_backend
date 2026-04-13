package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.dto.ReviewDTO.ReviewRequest;
import com.app.tastefrancesinhasbackend.dto.ReviewDTO.ReviewResponse;
import com.app.tastefrancesinhasbackend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/francesinhas/{francesinhaId}/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Lista todas las reviews de una francesinha aprobada.
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> findAll(@PathVariable Long francesinhaId) {
        return ResponseEntity.ok(reviewService.findByFrancesinha(francesinhaId));
    }

    // Publica una review. Cada usuario solo puede valorar una vez cada francesinha.
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReviewResponse> create(@PathVariable Long francesinhaId,
                                                 @Valid @RequestBody ReviewRequest request,
                                                 Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(francesinhaId, request, auth));
    }

    // TODO: borrado de reviews pendiente — hay que decidir si puede borrar solo el autor o también el admin
//    @DeleteMapping("/{reviewId}")
//    public ResponseEntity<Void> delete(@PathVariable Long francesinhaId,
//                                       @PathVariable Long reviewId,
//                                       Authentication auth) {
//        reviewService.delete(francesinhaId, reviewId, auth);
//        return ResponseEntity.noContent().build();
//    }
}
