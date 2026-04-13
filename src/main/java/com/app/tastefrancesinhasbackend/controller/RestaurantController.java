package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.config.ApiConstants;
import com.app.tastefrancesinhasbackend.dto.PageResponse;
import com.app.tastefrancesinhasbackend.dto.RestaurantDTO.RestaurantRequest;
import com.app.tastefrancesinhasbackend.dto.RestaurantDTO.RestaurantResponse;
import com.app.tastefrancesinhasbackend.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/restaurants", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // GET /restaurants — público
    // Filtros opcionales: ?name=cafe&city=oporto
    // Paginación: ?page=0&size=10&sort=name,asc
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Map<String, Object>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(restaurantService.findAll(name, city, pageable), ApiConstants.RESTAURANTS_CONTENT_KEY));
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