package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.entity.Restaurant;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class RestaurantDTO {

    public record RestaurantRequest(
            @NotBlank String name,
            String address,
            @NotBlank String city,
            String phone
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RestaurantResponse(
            Long id,
            String name,
            String address,
            String city,
            String phone,
            String proposedByEmail,
            Long totalFrancesinhas,
            LocalDateTime createdAt
    ) {}

    // Mapea la entidad Restaurant al record de respuesta
    public static RestaurantResponse responsePublic(Restaurant r) {
        return new RestaurantResponse(
                r.getId(),
                r.getName(),
                r.getAddress(),
                r.getCity(),
                r.getPhone(),
                null,
                r.getTotalFrancesinhas(),
                r.getCreatedAt()
        );
    }

    public static RestaurantResponse responsePrivate(Restaurant r) {
        return new RestaurantResponse(
                r.getId(),
                r.getName(),
                r.getAddress(),
                r.getCity(),
                r.getPhone(),
                r.getProposedBy().getEmail(),
                r.getTotalFrancesinhas(),
                r.getCreatedAt()
        );
    }
}