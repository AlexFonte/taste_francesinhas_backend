package com.app.tastefrancesinhasbackend.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PageResponse {

    private long total;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
}
