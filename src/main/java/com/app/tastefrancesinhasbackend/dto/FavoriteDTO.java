package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.entity.Favorite;

import java.time.LocalDateTime;

public class FavoriteDTO {

    public record FavoriteResponse(
            Long id,
            FrancesinhaDTO.FrancesinhaResponse francesinha,
            LocalDateTime createdAt
    ) {}

    public record ToggleResponse(
            boolean added,
            Long francesinhaId
    ) {}

    public static FavoriteResponse response(Favorite f, String coverPhotoUrl) {
        return new FavoriteResponse(
                f.getId(),
                FrancesinhaDTO.responsePublic(f.getFrancesinha(), coverPhotoUrl, null),
                f.getCreatedAt()
        );
    }
}
