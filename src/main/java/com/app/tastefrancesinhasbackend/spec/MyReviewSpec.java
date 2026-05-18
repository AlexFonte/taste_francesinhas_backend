package com.app.tastefrancesinhasbackend.spec;

import com.app.tastefrancesinhasbackend.entity.Review;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MyReviewSpec {

    private MyReviewSpec() {}

    // Filtros para "mis reviews" del perfil. Buscar por el usuario logeado: user.id = X y francesinha.status = ACCEPTED
    // (en /profile/reviews no enseñamos reviews de propuestas pendientes/rechazadas).
    // name/city/type son opcionales y van contra la francesinha asociada.
    public static Specification<Review> withFilters(Long userId, String name, FrancesinhaType type, String city) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtros fijos: user del usuario autenticado y francesinha ACCEPTED.
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // Necesitamos el join a francesinha para los filtros sobre ella.
            Join<Object, Object> francesinha = root.join("francesinha", JoinType.INNER);
            predicates.add(cb.equal(francesinha.get("status"), FrancesinhaStatus.ACCEPTED));

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(francesinha.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (type != null) {
                predicates.add(cb.equal(francesinha.get("type"), type));
            }

            // Solo hacemos JOIN al restaurante si se filtra por ciudad (igual que en FrancesinhaSpec).
            if (city != null && !city.isBlank()) {
                query.distinct(true);
                Join<Object, Object> restaurant = francesinha.join("restaurant", JoinType.INNER);
                predicates.add(cb.like(cb.lower(restaurant.get("city")), "%" + city.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}