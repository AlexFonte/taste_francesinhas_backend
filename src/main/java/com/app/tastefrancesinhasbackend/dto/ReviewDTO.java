package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.entity.Review;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReviewDTO {

    public record ReviewRequest(
            @NotNull @Min(1) @Max(5) Short scoreFlavor,
            @NotNull @Min(1) @Max(5) Short scoreSauce,
            @NotNull @Min(1) @Max(5) Short scoreBread,
            @NotNull @Min(1) @Max(5) Short scorePresentation,
            @NotBlank @Size(max = 500) String comment,
            // Cuando viene del flujo de proponer la francesinha esta en estado PENDING.
            // En una review normal (francesinha ya aprobada) llegara como false o null.
            Boolean propuesta
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ReviewResponse(
            Long id,
            Short scoreFlavor,
            Short scoreSauce,
            Short scoreBread,
            Short scorePresentation,
            BigDecimal avgScore,
            String comment,
            String userName,
            LocalDateTime createdAt
    ) {}

    public static ReviewResponse responsePublic(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getScoreFlavor(),
                r.getScoreSauce(),
                r.getScoreBread(),
                r.getScorePresentation(),
                r.getAvgScore(),
                r.getComment(),
                r.getUser().getName(),
                r.getCreatedAt()
        );
    }
}