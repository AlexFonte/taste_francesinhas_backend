package com.app.tastefrancesinhasbackend.spec;

import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class FrancesinhaSpec {

    private FrancesinhaSpec() {}

    public static Specification<Francesinha> withFilters(FrancesinhaStatus status, String name,
                                                         FrancesinhaType type, String city) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), status));

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            // Solo se hace el JOIN a restaurant si se filtra por ciudad
            if (city != null && !city.isBlank()) {
                Join<Object, Object> restaurant = root.join("restaurant", JoinType.INNER);
                predicates.add(cb.like(cb.lower(restaurant.get("city")), "%" + city.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
