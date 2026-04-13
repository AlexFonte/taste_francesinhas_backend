package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.dto.ReviewDTO;
import com.app.tastefrancesinhasbackend.dto.ReviewDTO.ReviewRequest;
import com.app.tastefrancesinhasbackend.dto.ReviewDTO.ReviewResponse;
import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.Review;
import com.app.tastefrancesinhasbackend.entity.User;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.exception.ConflictException;
import com.app.tastefrancesinhasbackend.exception.ResourceNotFoundException;
import com.app.tastefrancesinhasbackend.repository.FrancesinhaRepository;
import com.app.tastefrancesinhasbackend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FrancesinhaRepository francesinhaRepository;

    // GET /francesinhas/{id}/reviews — público, lista reviews de una francesinha aceptada
    @Transactional(readOnly = true)
    public List<ReviewResponse> findByFrancesinha(Long francesinhaId) {
        francesinhaRepository.findByIdAndStatus(francesinhaId, FrancesinhaStatus.ACCEPTED)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + francesinhaId));

        return reviewRepository.findByFrancesinhaId(francesinhaId)
                .stream()
                .map(ReviewDTO::response)
                .toList();
    }

    // POST /francesinhas/{id}/reviews — autenticado, crea una review
    // Un usuario solo puede dejar una review por francesinha
    @Transactional
    public ReviewResponse create(Long francesinhaId, ReviewRequest request, Authentication auth) {
        User user = (User) auth.getPrincipal();

        Francesinha francesinha = francesinhaRepository.findByIdAndStatus(francesinhaId, FrancesinhaStatus.ACCEPTED)
                .orElseThrow(() -> new ResourceNotFoundException("Francesinha no encontrada: " + francesinhaId));

        if (reviewRepository.existsByFrancesinhaIdAndUserId(francesinhaId, user.getId())) {
            throw new ConflictException("Ya has valorado esta francesinha");
        }

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
        recalcularScore(francesinha, reviewAvg);

        return ReviewDTO.response(review);
    }

    // TODO: borrado de reviews desactivado temporalmente — pendiente de rediseñar la lógica de autorización
//    @Transactional
//    public void delete(Long francesinhaId, Long reviewId, Authentication auth) {
//        User user = (User) auth.getPrincipal();
//
//        Review review = reviewRepository.findByFrancesinhaIdAndUserId(francesinhaId, user.getId())
//                .orElseThrow(() -> new ResourceNotFoundException("Review no encontrada"));
//
//        if (!review.getId().equals(reviewId)) {
//            throw new ResourceNotFoundException("Review no encontrada");
//        }
//
//        boolean isAdmin = user.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//
//        if (!isAdmin && !review.getUser().getId().equals(user.getId())) {
//            throw new UnauthorizedException("No tienes permiso para eliminar esta review");
//        }
//
//        Francesinha francesinha = review.getFrancesinha();
//        reviewRepository.delete(review);
//        // TODO: recalcularScore necesita lógica de resta — firma actual: recalcularScore(Francesinha, BigDecimal)
//    }

    // Actualiza avgScore y totalReviews en la francesinha de forma incremental.
    // Evita recargar todas las reviews de BD: usa la media acumulada + la nueva review.
    // fórmula: nuevoAvg = (avgActual * totalActual + nuevaMedia) / (totalActual + 1)
    private void recalcularScore(Francesinha francesinha, BigDecimal newReviewAvg) {
        long nuevoTotal = francesinha.getTotalReviews() + 1;
        BigDecimal nuevoAvg = francesinha.getAvgScore()
                .multiply(BigDecimal.valueOf(francesinha.getTotalReviews()))
                .add(newReviewAvg)
                .divide(BigDecimal.valueOf(nuevoTotal), 2, RoundingMode.HALF_UP);

        francesinha.setTotalReviews(nuevoTotal);
        francesinha.setAvgScore(nuevoAvg);
        francesinhaRepository.save(francesinha);
    }
}