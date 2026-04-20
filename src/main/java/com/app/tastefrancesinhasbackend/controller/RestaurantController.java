package com.app.tastefrancesinhasbackend.controller;

import com.app.tastefrancesinhasbackend.config.ApiConstants;
import com.app.tastefrancesinhasbackend.dto.PageResponse;
import com.app.tastefrancesinhasbackend.dto.RestaurantDTO.RestaurantRequest;
import com.app.tastefrancesinhasbackend.dto.RestaurantDTO.RestaurantResponse;
import com.app.tastefrancesinhasbackend.service.RestaurantService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/restaurants", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Listado y propuesta de restaurantes")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @Operation(summary = "Listar restaurantes", description = "Filtros opcionales: name, city.")
    @ApiResponse(responseCode = "200", description = "Listado paginado")
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Map<String, Object>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(restaurantService.findAll(name, city, pageable), ApiConstants.RESTAURANTS_CONTENT_KEY));
    }

    @Operation(summary = "Detalle de un restaurante", description = "Público.")
    @ApiResponse(responseCode = "200", description = "Restaurante encontrado")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.findById(id));
    }

    @Operation(summary = "Crear restaurante", description = "Autenticado. El usuario autenticado queda registrado como quien propuso el restaurante.")
    @ApiResponse(responseCode = "201", description = "Restaurante creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestaurantResponse> create(@Valid @RequestBody RestaurantRequest request,
                                                     Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.create(request, auth));
    }
}
