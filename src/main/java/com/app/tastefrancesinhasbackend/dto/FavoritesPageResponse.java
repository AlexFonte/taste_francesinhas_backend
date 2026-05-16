package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.dto.FavoriteDTO.FavoriteResponse;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonPropertyOrder({"favorites", "total", "totalPages", "pageNumber", "pageSize"})
public class FavoritesPageResponse extends PageResponse {

    private List<FavoriteResponse> favorites;

    public static FavoritesPageResponse of(Page<FavoriteResponse> page) {
        return FavoritesPageResponse.builder()
                .favorites(page.getContent())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }
}
