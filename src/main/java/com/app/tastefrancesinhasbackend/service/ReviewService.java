package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.ReviewDTO;
import com.app.tastefrancesinhasbackend.dto.ReviewDTO.ReviewRequest;
import com.app.tastefrancesinhasbackend.dto.ReviewDTO.ReviewResponse;
import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.Review;
import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.exception.ResourceNotFoundException;
import com.app.tastefrancesinhasbackend.repository.FrancesinhaRepository;
import com.app.tastefrancesinhasbackend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FrancesinhaRepository francesinhaRepository;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> findByFrancesinha(Long francesinhaId, Pageable pageable) {
        francesinhaRepository.findByIdAndStatus(francesinhaId, FrancesinhaStatus.ACCEPTED)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + francesinhaId));

        return reviewRepository.findByFrancesinhaId(francesinhaId, pageable)
                .map(ReviewDTO::responsePublic);
    }

    // POST /francesinhas/{id}/reviews - autenticado, crea una review
    @Transactional
    public ReviewResponse create(Long francesinhaId, ReviewRequest request, Authentication auth) {
        User user = (User) auth.getPrincipal();

        // Si la review viene del flujo de proponer, la francesinha esta PENDING; si no, ACCEPTED.
        FrancesinhaStatus searchByStatus = Boolean.TRUE.equals(request.propuesta())
                ? FrancesinhaStatus.PENDING
                : FrancesinhaStatus.ACCEPTED;

        Francesinha francesinha = francesinhaRepository.findByIdAndStatus(francesinhaId, searchByStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + francesinhaId));

        BigDecimal reviewAvg = BigDecimal.valueOf(
                (request.scoreFlavor() + request.scoreSauce() + request.scoreBread() + request.scorePresentation()) / 4.0
        ).setScale(2, RoundingMode.HALF_UP);

        Review review = Review.builder()
                .francesinha(francesinha)
                .user(user)
                .scoreFlavor(request.scoreFlavor())
                .scoreSauce(request.scoreSauce())
                .scoreBread(request.scoreBread())
                .scorePresentation(request.scorePresentation())
                .avgScore(reviewAvg)
                .comment(request.comment())
                .build();

        reviewRepository.save(review);
        // La BD suma todos los avgScore de las reviews (incluida la nueva) y recalcula la media
        francesinhaRepository.updateScore(francesinha.getId());

        return ReviewDTO.responsePublic(review);
    }

}