package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
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
            Long restaurantId,
            String restaurantName,
            String proposedByEmail,
            LocalDateTime createdAt
    ) {}

    // Mapea la entidad Francesinha al record de respuesta
    public static FrancesinhaResponse response(Francesinha f) {
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
                f.getRestaurant().getId(),
                f.getRestaurant().getName(),
                f.getProposedBy().getEmail(),
                f.getCreatedAt()
        );
    }
}