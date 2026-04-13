package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaRequest;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaResponse;
import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaStatusRequest;
import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.Restaurant;
import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import com.app.tastefrancesinhasbackend.exception.BadRequestException;
import com.app.tastefrancesinhasbackend.exception.ResourceNotFoundException;
import com.app.tastefrancesinhasbackend.repository.FrancesinhaRepository;
import com.app.tastefrancesinhasbackend.repository.RestaurantRepository;
import com.app.tastefrancesinhasbackend.spec.FrancesinhaSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FrancesinhaService {

    private final FrancesinhaRepository francesinhaRepository;
    private final RestaurantRepository restaurantRepository;

    // Devuelve las francesinhas aprobadas. Los filtros son opcionales: sin ninguno devuelve todas.
    @Transactional(readOnly = true)
    public Page<FrancesinhaResponse> findAllAccepted(String name, String city, FrancesinhaType type,
                                                     Pageable pageable) {
        return francesinhaRepository.findAll(
                        FrancesinhaSpec.withFilters(FrancesinhaStatus.ACCEPTED, name, type, city), pageable)
                .map(FrancesinhaDTO::response);
    }

    // Busca una francesinha aprobada por id. Si no existe o no está aprobada, lanza 404.
    @Transactional(readOnly = true)
    public FrancesinhaResponse findById(Long id) {
        return francesinhaRepository.findByIdAndStatus(id, FrancesinhaStatus.ACCEPTED)
                .map(FrancesinhaDTO::response)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + id));
    }

    // Lista las francesinhas que el admin todavía no ha revisado.
    @Transactional(readOnly = true)
    public Page<FrancesinhaResponse> findAllPending(Pageable pageable) {
        return francesinhaRepository.findAll(
                        FrancesinhaSpec.withFilters(FrancesinhaStatus.PENDING, null, null, null), pageable)
                .map(FrancesinhaDTO::response);
    }

    // Igual que findById pero sin filtrar por estado, para que el admin pueda ver cualquier francesinha.
    @Transactional(readOnly = true)
    public FrancesinhaResponse findByIdForAdmin(Long id) {
        return francesinhaRepository.findById(id)
                .map(FrancesinhaDTO::response)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + id));
    }

    // El admin aprueba o rechaza una francesinha pendiente. No se puede volver a poner en PENDING.
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

    // Un usuario propone una nueva francesinha. Queda en PENDING hasta que un admin la revise.
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