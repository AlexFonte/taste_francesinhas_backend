package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.entity.Favorite;

import java.time.LocalDateTime;

public class FavoriteDTO {

    public record FavoriteResponse(
            Long francesinhaId,
            String francesinhaNombre,
            LocalDateTime createdAt
    ) {}

    public record ToggleResponse(
            boolean added,
            Long francesinhaId
    ) {}

    // Mapea la entidad Favorite al record de respuesta
    public static FavoriteResponse response(Favorite f) {
        return new FavoriteResponse(
                f.getFrancesinha().getId(),
                f.getFrancesinha().getName(),
                f.getCreatedAt()
        );
    }
}