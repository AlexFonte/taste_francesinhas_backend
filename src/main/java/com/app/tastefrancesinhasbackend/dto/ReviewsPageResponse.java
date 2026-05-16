package com.app.tastefrancesinhasbackend.dto;

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
@JsonPropertyOrder({"reviews", "total", "totalPages", "pageNumber", "pageSize"})
public class ReviewsPageResponse<T> extends PageResponse {

    private List<T> reviews;

    public static <T> ReviewsPageResponse<T> of(Page<T> page) {
        return ReviewsPageResponse.<T>builder()
                .reviews(page.getContent())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }
}
