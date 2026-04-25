package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
            String proposedByEmail,
            LocalDateTime createdAt,
            RestaurantDTO.RestaurantResponse restaurant
    ) {}

    public static FrancesinhaResponse responsePublic(Francesinha f) {
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
                null,
                f.getCreatedAt(),
                RestaurantDTO.responsePublic(f.getRestaurant())
        );
    }

    public static FrancesinhaResponse responsePrivate(Francesinha f) {
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
                f.getProposedBy().getEmail(),
                f.getCreatedAt(),
                RestaurantDTO.responsePrivate(f.getRestaurant())
        );
    }
}