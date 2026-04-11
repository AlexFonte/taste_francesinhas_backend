package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.FavoriteDTO;
import com.app.tastefrancesinhasbackend.dto.FavoriteDTO.FavoriteResponse;
import com.app.tastefrancesinhasbackend.dto.FavoriteDTO.ToggleResponse;
import com.app.tastefrancesinhasbackend.entity.Favorite;
import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.exception.ResourceNotFoundException;
import com.app.tastefrancesinhasbackend.repository.FavoriteRepository;
import com.app.tastefrancesinhasbackend.repository.FrancesinhaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final FrancesinhaRepository francesinhaRepository;

    // GET /favorites — solo USER, lista los favoritos del usuario autenticado
    @Transactional(readOnly = true)
    public List<FavoriteResponse> findByUser(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return favoriteRepository.findByUserId(user.getId())
                .stream()
                .map(FavoriteDTO::response)
                .toList();
    }

    // POST /favorites/{francesinhaId} — solo USER
    // Si ya es favorito lo elimina (toggle), si no lo añade
    @Transactional
    public ToggleResponse toggle(Long francesinhaId, Authentication auth) {
        User user = (User) auth.getPrincipal();

        // Solo se pueden marcar como favorito francesinhas aceptadas
        Francesinha francesinha = francesinhaRepository.findByIdAndStatus(francesinhaId, FrancesinhaStatus.ACCEPTED)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + francesinhaId));

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndFrancesinhaId(user.getId(), francesinhaId);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return new ToggleResponse(false, francesinhaId);
        }

        favoriteRepository.save(Favorite.builder()
                .user(user)
                .francesinha(francesinha)
                .build());

        return new ToggleResponse(true, francesinhaId);
    }
}