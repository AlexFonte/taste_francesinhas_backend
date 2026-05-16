package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.dto.FrancesinhaDTO.FrancesinhaResponse;
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
@JsonPropertyOrder({"francesinhas", "total", "totalPages", "pageNumber", "pageSize"})
public class FrancesinhasPageResponse extends PageResponse {

    private List<FrancesinhaResponse> francesinhas;

    public static FrancesinhasPageResponse of(Page<FrancesinhaResponse> page) {
        return FrancesinhasPageResponse.builder()
                .francesinhas(page.getContent())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }
}
