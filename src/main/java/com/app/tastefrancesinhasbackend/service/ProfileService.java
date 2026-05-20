package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.ProfileDTO;
import com.app.tastefrancesinhasbackend.dto.ProfileDTO.MyProposalResponse;
import com.app.tastefrancesinhasbackend.dto.ProfileDTO.MyReviewResponse;
import com.app.tastefrancesinhasbackend.dto.ProfileDTO.UserStatsResponse;
import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.Review;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import com.app.tastefrancesinhasbackend.repository.FrancesinhaRepository;
import com.app.tastefrancesinhasbackend.repository.ReviewRepository;
import com.app.tastefrancesinhasbackend.security.CurrentUserContext;
import com.app.tastefrancesinhasbackend.spec.MyReviewSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ReviewRepository reviewRepository;
    private final FrancesinhaRepository francesinhaRepository;
    private final CurrentUserContext currentUser;

    // Devuelve cuantas reviews y propuestas tiene el usuario autenticado.
    @Transactional(readOnly = true)
    public UserStatsResponse getStats() {
        Long userId = currentUser.id();
        long reviewsCount = reviewRepository.countByUserId(userId);
        long proposalsCount = francesinhaRepository.countByProposedById(userId);
        return new UserStatsResponse(reviewsCount, proposalsCount);
    }

    // Listado paginado de las reviews del usuario con datos de la francesinha.
    // Solo devolvemos reviews de francesinhas ACCEPTED (lo fuerza la Spec); en el perfil no tiene
    // mostrar valoraciones de propuestas pendientes o rechazadasy con el filtro
    // filtros opcionales que viajan en la query string del controlador.
    @Transactional(readOnly = true)
    public Page<MyReviewResponse> getMyReviews(String name, FrancesinhaType type, String city, Pageable pageable) {
        return reviewRepository.findAll(MyReviewSpec.withFilters(currentUser.id(), name, type, city), pageable)
                .map(ProfileDTO::responseMyReview);
    }

    // Propuestas del usuario. Si status viene, filtramos por ese estado concreto, si no, devolvemos todas
    @Transactional(readOnly = true)
    public Page<MyProposalResponse> getMyProposals(FrancesinhaStatus status, Pageable pageable) {
        Long userId = currentUser.id();

        Page<Francesinha> page = status != null
                ? francesinhaRepository.findByProposedByIdAndStatus(userId, status, pageable)
                : francesinhaRepository.findByProposedById(userId, pageable);


        Set<Long> francesinhaIds = page.getContent().stream()
                .map(Francesinha::getId)
                .collect(Collectors.toSet());

        Map<Long, Review> firstReviewByFrancesinha = new HashMap<>();
        if (!francesinhaIds.isEmpty()) {
            List<Review> reviews = reviewRepository
                    .findByUserIdAndFrancesinhaIdInOrderByCreatedAtAsc(userId, francesinhaIds);
            // Como vienen ordenadas ASC, el primer putIfAbsent guarda la mas antigua para cada francesinha.
            reviews.forEach(r -> firstReviewByFrancesinha.putIfAbsent(r.getFrancesinha().getId(), r));
        }

        return page.map(f -> ProfileDTO.responseMyProposal(f, firstReviewByFrancesinha.get(f.getId())));
    }
}