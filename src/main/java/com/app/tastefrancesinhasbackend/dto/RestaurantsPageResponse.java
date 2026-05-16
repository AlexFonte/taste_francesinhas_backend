package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.dto.RestaurantDTO.RestaurantResponse;
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
@JsonPropertyOrder({"restaurants", "total", "totalPages", "pageNumber", "pageSize"})
public class RestaurantsPageResponse extends PageResponse {

    private List<RestaurantResponse> restaurants;

    public static RestaurantsPageResponse of(Page<RestaurantResponse> page) {
        return RestaurantsPageResponse.builder()
                .restaurants(page.getContent())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }
}
