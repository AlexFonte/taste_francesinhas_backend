package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.RestaurantDTO;
import com.app.tastefrancesinhasbackend.dto.RestaurantDTO.RestaurantRequest;
import com.app.tastefrancesinhasbackend.dto.RestaurantDTO.RestaurantResponse;
import com.app.tastefrancesinhasbackend.entity.Restaurant;
import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.exception.ResourceNotFoundException;
import com.app.tastefrancesinhasbackend.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    // GET /restaurants — público
    @Transactional(readOnly = true)
    public List<RestaurantResponse> findAll() {
        return restaurantRepository.findAll()
                .stream()
                .map(RestaurantDTO::response)
                .toList();
    }

    // GET /restaurants/{id} — público
    @Transactional(readOnly = true)
    public RestaurantResponse findById(Long id) {
        return restaurantRepository.findById(id)
                .map(RestaurantDTO::response)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante no encontrado: " + id));
    }

    // POST /restaurants — autenticado
    // proposedBy solo registra quién hizo la propuesta, no implica propiedad
    @Transactional
    public RestaurantResponse create(RestaurantRequest request, Authentication auth) {
        User proposedBy = (User) auth.getPrincipal();
        return RestaurantDTO.response(restaurantRepository.save(buildRestaurant(request, proposedBy)));
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