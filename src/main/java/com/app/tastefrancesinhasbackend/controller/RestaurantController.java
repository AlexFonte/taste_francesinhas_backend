package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.dto.RestaurantDTO.RestaurantRequest;
import com.app.tastefrancesinhasbackend.dto.RestaurantDTO.RestaurantResponse;
import com.app.tastefrancesinhasbackend.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/restaurants", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // GET /restaurants — público
    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<RestaurantResponse>> findAll() {
        return ResponseEntity.ok(restaurantService.findAll());
    }

    // GET /restaurants/{id} — público
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.findById(id));
    }

    // POST /restaurants — autenticado
    // El usuario autenticado queda registrado como quien propuso el restaurante
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestaurantResponse> create(@Valid @RequestBody RestaurantRequest request,
                                                     Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.create(request, auth));
    }
}