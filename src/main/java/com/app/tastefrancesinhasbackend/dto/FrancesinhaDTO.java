package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.Review;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class FrancesinhaDTO {

    public record FrancesinhaRequest(
            @NotNull Long restaurantId,
            @NotBlank String name,
            String description,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            boolean hasEgg,
            boolean hasFries,
            boolean isSpicy,
            @NotNull FrancesinhaType type
    ) {}

    public record FrancesinhaStatusRequest(
            @NotNull FrancesinhaStatus status
    ) {}

    // Contadores agregados que pinta el dashboard de admin (4 cards arriba)
    public record StatsResponse(
            long pending,
            long accepted,
            long rejected,
            long total
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FrancesinhaResponse(
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
            String proposedByEmail,
            String coverPhotoUrl,
            List<String> photoUrls,
            LocalDateTime createdAt,
            RestaurantDTO.RestaurantResponse restaurant
    ) {}

    public static PendingFrancesinhaWithReviewResponse pendingWithReview(Francesinha f, String coverPhotoUrl,
                                                                         List<String> photoUrls, Review review) {
        return new PendingFrancesinhaWithReviewResponse(
                responsePrivate(f, coverPhotoUrl, photoUrls),
                review != null ? ReviewDTO.responsePublic(review) : null
        );
    }

    public record PendingFrancesinhaWithReviewResponse(
            FrancesinhaResponse francesinha,
            ReviewDTO.ReviewResponse review
    ) {
    }

    public static FrancesinhaResponse responsePublic(Francesinha f, String coverPhotoUrl, List<String> photoUrls) {
        return new FrancesinhaResponse(
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
                null,
                coverPhotoUrl,
                photoUrls,
                f.getCreatedAt(),
                RestaurantDTO.responsePublic(f.getRestaurant())
        );
    }

    public static FrancesinhaResponse responsePrivate(Francesinha f, String coverPhotoUrl, List<String> photoUrls) {
        return new FrancesinhaResponse(
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
                f.getProposedBy().getEmail(),
                coverPhotoUrl,
                photoUrls,
                f.getCreatedAt(),
                RestaurantDTO.responsePrivate(f.getRestaurant())
        );
    }
}