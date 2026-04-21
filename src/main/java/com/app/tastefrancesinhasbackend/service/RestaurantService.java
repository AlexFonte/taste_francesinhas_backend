package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.RestaurantDTO;
import com.app.tastefrancesinhasbackend.dto.RestaurantDTO.RestaurantRequest;
import com.app.tastefrancesinhasbackend.dto.RestaurantDTO.RestaurantResponse;
import com.app.tastefrancesinhasbackend.entity.Restaurant;
import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.exception.ResourceNotFoundException;
import com.app.tastefrancesinhasbackend.repository.RestaurantRepository;
import com.app.tastefrancesinhasbackend.spec.RestaurantSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    // Lista todos los restaurantes. Se puede filtrar por nombre o ciudad.
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> findAll(String name, String city, Pageable pageable) {

        Specification<Restaurant> spec = RestaurantSpec.withFilters(name, city);
        return restaurantRepository.findAll(spec, pageable)
                .map(RestaurantDTO::responsePublic);
    }

    // Devuelve un restaurante por id. Si no existe, lanza 404.
    @Transactional(readOnly = true)
    public RestaurantResponse findById(Long id) {
        return restaurantRepository.findById(id)
                .map(RestaurantDTO::responsePublic)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante no encontrado: " + id));
    }

    // Registra un restaurante nuevo. Guarda quién lo propuso, pero eso no implica permisos especiales.
    @Transactional
    public RestaurantResponse create(RestaurantRequest request, Authentication auth) {
        User proposedBy = (User) auth.getPrincipal();
        return RestaurantDTO.responsePublic(restaurantRepository.save(buildRestaurant(request, proposedBy)));
    }

    private Restaurant buildRestaurant(RestaurantRequest request, User proposedBy) {
        return Restaurant.builder()
                .proposedBy(proposedBy)
                .name(request.name())
                .address(request.address())
                .city(request.city())
                .phone(request.phone())
                .build();
    }
}