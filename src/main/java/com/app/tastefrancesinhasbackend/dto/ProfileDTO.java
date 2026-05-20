package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.Review;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProfileDTO {

    public static MyReviewResponse responseMyReview(Review r) {
        return new MyReviewResponse(
                r.getId(),
                r.getScoreFlavor(),
                r.getScoreSauce(),
                r.getScoreBread(),
                r.getScorePresentation(),
                r.getAvgScore(),
                r.getComment(),
                r.getPhotoUrl(),
                r.getCreatedAt(),
                r.getFrancesinha().getId(),
                r.getFrancesinha().getName(),
                r.getFrancesinha().getType(),
                r.getFrancesinha().getStatus(),
                r.getFrancesinha().getRestaurant().getName(),
                r.getFrancesinha().getRestaurant().getCity()
        );
    }

    public static MyProposalResponse responseMyProposal(Francesinha f, Review userReview) {
        return new MyProposalResponse(
                f.getId(),
                f.getName(),
                f.getDescription(),
                f.getPrice(),
                f.isHasEgg(),
                f.isHasFries(),
                f.isSpicy(),
                f.getType(),
                f.getStatus(),
                f.getTotalReviews(),
                f.getAvgScore(),
                f.getAvgFlavor(),
                f.getAvgSauce(),
                f.getAvgBread(),
                f.getAvgPresentation(),
                f.getCreatedAt(),
                RestaurantDTO.responsePublic(f.getRestaurant()),
                userReview != null ? toProposalReview(userReview) : null
        );
    }

    private static ProposalReview toProposalReview(Review r) {
        return new ProposalReview(
                r.getId(),
                r.getScoreFlavor(),
                r.getScoreSauce(),
                r.getScoreBread(),
                r.getScorePresentation(),
                r.getAvgScore(),
                r.getComment(),
                r.getPhotoUrl(),
                r.getCreatedAt()
        );
    }

    // Contadores agregados que pinta la pantalla de perfil del usuario.
    public record UserStatsResponse(
            long reviewsCount,
            long proposalsCount
    ) {
    }

    // Review del usuario con datos minimos de la francesinha valorada,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MyReviewResponse(
            Long id,
            Short scoreFlavor,
            Short scoreSauce,
            Short scoreBread,
            Short scorePresentation,
            BigDecimal avgScore,
            String comment,
            String photoUrl,
            LocalDateTime createdAt,
            Long francesinhaId,
            String francesinhaName,
            FrancesinhaType francesinhaType,
            FrancesinhaStatus francesinhaStatus,
            String restaurantName,
            String restaurantCity
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MyProposalResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            boolean hasEgg,
            boolean hasFries,
            boolean isSpicy,
            FrancesinhaType type,
            FrancesinhaStatus status,
            Long totalReviews,
            BigDecimal avgScore,
            BigDecimal avgFlavor,
            BigDecimal avgSauce,
            BigDecimal avgBread,
            BigDecimal avgPresentation,
            LocalDateTime createdAt,
            RestaurantDTO.RestaurantResponse restaurant,
            ProposalReview userReview
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ProposalReview(
            Long id,
            Short scoreFlavor,
            Short scoreSauce,
            Short scoreBread,
            Short scorePresentation,
            BigDecimal avgScore,
            String comment,
            String photoUrl,
            LocalDateTime createdAt
    ) {
    }
}