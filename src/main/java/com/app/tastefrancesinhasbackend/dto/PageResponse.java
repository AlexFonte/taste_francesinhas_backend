package com.app.tastefrancesinhasbackend.dto;

import org.springframework.data.domain.Page;

import java.util.LinkedHashMap;
import java.util.Map;

public class PageResponse {

    private PageResponse() {}

    public static <T> Map<String, Object> of(Page<T> page, String contentKey) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put(contentKey, page.getContent());
        response.put("total", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("pageNumber", page.getNumber());
        response.put("pageSize", page.getSize());
        return response;
    }
}
