package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaRequest;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaResponse;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaStatusRequest;
import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.Restaurant;
import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.exception.BadRequestException;
import com.app.tastefrancesinhasbackend.exception.ResourceNotFoundException;
import com.app.tastefrancesinhasbackend.repository.FrancesinhaRepository;
import com.app.tastefrancesinhasbackend.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FrancesinhaService {

    private final FrancesinhaRepository francesinhaRepository;
    private final RestaurantRepository restaurantRepository;

    // GET /francesinhas — público, solo las aceptadas
    @Transactional(readOnly = true)
    public List<FrancesinhaResponse> findAllAccepted() {
        return francesinhaRepository.findByStatus(FrancesinhaStatus.ACCEPTED)
                .stream()
                .map(FrancesinhaDTO::response)
                .toList();
    }

    // GET /francesinhas/{id} — público, solo si está aceptada
    // El filtro por estado se aplica en la query SQL, no en memoria
    @Transactional(readOnly = true)
    public FrancesinhaResponse findById(Long id) {
        return francesinhaRepository.findByIdAndStatus(id, FrancesinhaStatus.ACCEPTED)
                .map(FrancesinhaDTO::response)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + id));
    }

    // GET /francesinhas/pending — solo ADMIN, lista las pendientes de revisión
    @Transactional(readOnly = true)
    public List<FrancesinhaResponse> findAllPending() {
        return francesinhaRepository.findByStatus(FrancesinhaStatus.PENDING)
                .stream()
                .map(FrancesinhaDTO::response)
                .toList();
    }

    // GET /francesinhas/{id}/detail — solo ADMIN, detalle sin filtro de estado
    @Transactional(readOnly = true)
    public FrancesinhaResponse findByIdForAdmin(Long id) {
        return francesinhaRepository.findById(id)
                .map(FrancesinhaDTO::response)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + id));
    }

    // PATCH /francesinhas/{id}/status — solo ADMIN, cambia el estado
    // Solo se permiten las transiciones PENDING → ACCEPTED y PENDING → REJECTED
    @Transactional
    public FrancesinhaResponse updateStatus(Long id, FrancesinhaStatusRequest request) {
        if (request.status() == FrancesinhaStatus.PENDING) {
            throw new BadRequestException("El estado no puede ser PENDING");
        }

        Francesinha francesinha = francesinhaRepository.findByIdAndStatus(id, FrancesinhaStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha pendiente no encontrada: " + id));

        francesinha.setStatus(request.status());

        return FrancesinhaDTO.response(francesinhaRepository.save(francesinha));
    }

    // POST /francesinhas — autenticado, propone una nueva francesinha
    // La francesinha se crea en estado PENDING hasta que un ADMIN la acepte o rechace
    @Transactional
    public FrancesinhaResponse propose(FrancesinhaRequest request, Authentication auth) {
        User proposedBy = (User) auth.getPrincipal();

        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante no encontrado: " + request.restaurantId()));

        Francesinha francesinha = Francesinha.builder()
                .restaurant(restaurant)
                .proposedBy(proposedBy)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .hasEgg(request.hasEgg())
                .hasFries(request.hasFries())
                .isSpicy(request.isSpicy())
                .type(request.type())
                .build();

        return FrancesinhaDTO.response(francesinhaRepository.save(francesinha));
    }
}