package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.ProfileDTO;
import com.app.tastefrancesinhasbackend.dto.ProfileDTO.MyProposalResponse;
import com.app.tastefrancesinhasbackend.dto.ProfileDTO.MyReviewResponse;
import com.app.tastefrancesinhasbackend.dto.ProfileDTO.UserStatsResponse;
import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.Review;
import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import com.app.tastefrancesinhasbackend.exception.UnauthorizedException;
import com.app.tastefrancesinhasbackend.repository.FrancesinhaRepository;
import com.app.tastefrancesinhasbackend.repository.ReviewRepository;
import com.app.tastefrancesinhasbackend.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final FrancesinhaRepository francesinhaRepository;

    // Devuelve cuantas reviews y propuestas tiene el usuario autenticado.
    @Transactional(readOnly = true)
    public UserStatsResponse getStats(String email) {
        User user = findUserByEmail(email);

        long reviewsCount   = reviewRepository.countByUserId(user.getId());
        long proposalsCount = francesinhaRepository.countByProposedById(user.getId());

        return new UserStatsResponse(reviewsCount, proposalsCount);
    }

    // Listado paginado de las reviews del usuario, con datos de la francesinha.
    // Solo devolvemos reviews de francesinhas ACCEPTED: en el perfil no tiene sentido
    // mostrar valoraciones de propuestas que aun estan pendientes o han sido rechazadas y con el filtro
    @Transactional(readOnly = true)
    public Page<MyReviewResponse> getMyReviews(String email, String name, FrancesinhaType type, String city, Pageable pageable) {
        User user = findUserByEmail(email);
        return reviewRepository.findAll(MyReviewSpec.withFilters(user.getId(), name, type, city), pageable)
                .map(ProfileDTO::responseMyReview);
    }

    // Propuestas del usuario. Si status viene, filtramos por ese estado concreto, si no, devolvemos todas
    @Transactional(readOnly = true)
    public Page<MyProposalResponse> getMyProposals(String email, FrancesinhaStatus status, Pageable pageable) {
        User user = findUserByEmail(email);

        Page<Francesinha> page = status != null
                ? francesinhaRepository.findByProposedByIdAndStatus(user.getId(), status, pageable)
                : francesinhaRepository.findByProposedById(user.getId(), pageable);


        Set<Long> francesinhaIds = page.getContent().stream()
                .map(Francesinha::getId)
                .collect(Collectors.toSet());

        Map<Long, Review> firstReviewByFrancesinha = new HashMap<>();
        if (!francesinhaIds.isEmpty()) {
            List<Review> reviews = reviewRepository
                    .findByUserIdAndFrancesinhaIdInOrderByCreatedAtAsc(user.getId(), francesinhaIds);
            // Como vienen ordenadas ASC, el primer putIfAbsent guarda la mas antigua para cada francesinha.
            reviews.forEach(r -> firstReviewByFrancesinha.putIfAbsent(r.getFrancesinha().getId(), r));
        }

        return page.map(f -> ProfileDTO.responseMyProposal(f, firstReviewByFrancesinha.get(f.getId())));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));
    }
}