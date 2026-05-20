package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.FavoriteDTO;
import com.app.tastefrancesinhasbackend.dto.FavoriteDTO.FavoriteResponse;
import com.app.tastefrancesinhasbackend.dto.FavoriteDTO.ToggleResponse;
import com.app.tastefrancesinhasbackend.entity.Favorite;
import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import com.app.tastefrancesinhasbackend.exception.ResourceNotFoundException;
import com.app.tastefrancesinhasbackend.repository.FavoriteRepository;
import com.app.tastefrancesinhasbackend.repository.FrancesinhaRepository;
import com.app.tastefrancesinhasbackend.repository.ReviewRepository;
import com.app.tastefrancesinhasbackend.security.CurrentUserContext;
import com.app.tastefrancesinhasbackend.spec.FavoriteSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final FrancesinhaRepository francesinhaRepository;
    private final ReviewRepository reviewRepository;
    private final CurrentUserContext currentUser;

    // Lista los favoritos del usuario autenticado. Se puede filtrar por nombre, tipo o ciudad.
    @Transactional(readOnly = true)
    public Page<FavoriteResponse> findByUser(String name, String city, FrancesinhaType type, Pageable pageable) {
        Specification<Favorite> spec = FavoriteSpec.withFilters(currentUser.id(), name, type, city);
        Page<Favorite> page = favoriteRepository.findAll(spec, pageable);

        List<Long> francesinhaIds = page.getContent().stream()
                .map(fav -> fav.getFrancesinha().getId())
                .toList();
        Map<Long, String> covers = francesinhaIds.isEmpty()
                ? Map.of()
                : reviewRepository.findCoverPhotoUrlsByFrancesinhaIds(francesinhaIds).stream()
                  .collect(Collectors.toMap(
                          row -> ((Number) row[0]).longValue(),
                          row -> (String) row[1]
                  ));

        return page.map(fav -> FavoriteDTO.response(fav, covers.get(fav.getFrancesinha().getId())));
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long francesinhaId) {
        return favoriteRepository.findByUserIdAndFrancesinhaId(currentUser.id(), francesinhaId).isPresent();
    }

    // Añade o quita una francesinha de favoritos. Si ya era favorita la elimina; si no, la añade.
    @Transactional
    public ToggleResponse toggle(Long francesinhaId) {
        Long userId = currentUser.id();
        // No tiene sentido guardar como favorita una francesinha que aún no está aprobada.
        Francesinha francesinha = francesinhaRepository.findByIdAndStatus(francesinhaId, FrancesinhaStatus.ACCEPTED)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + francesinhaId));

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndFrancesinhaId(userId, francesinhaId);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return new ToggleResponse(false, francesinhaId);
        }

        favoriteRepository.save(Favorite.builder()
                .user(currentUser.getUser())
                .francesinha(francesinha)
                .build());

        return new ToggleResponse(true, francesinhaId);
    }
}