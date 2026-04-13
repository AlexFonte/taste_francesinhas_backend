package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.entity.Favorite;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;

import java.time.LocalDateTime;

public class FavoriteDTO {

    public record FavoriteResponse(
            Long francesinhaId,
            String francesinhaName,
            FrancesinhaType francesinhaType,
            String restaurantCity,
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
                f.getFrancesinha().getType(),
                f.getFrancesinha().getRestaurant().getCity(),
                f.getCreatedAt()
        );
    }
}
