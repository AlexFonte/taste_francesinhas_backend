package com.app.tastefrancesinhasbackend.spec;

import com.app.tastefrancesinhasbackend.entity.Favorite;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class FavoriteSpec {

    private FavoriteSpec() {
    }

    public static Specification<Favorite> withFilters(Long userId, String name,
                                                      FrancesinhaType type, String city) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // Solo se hace el JOIN a francesinha si algún filtro lo necesita
            if (name != null || type != null || city != null) {
                query.distinct(true);
                Join<Object, Object> francesinha = root.join("francesinha", JoinType.INNER);

                if (name != null && !name.isBlank()) {
                    predicates.add(cb.like(cb.lower(francesinha.get("name")),
                            "%" + name.toLowerCase() + "%"));
                }

                if (type != null) {
                    predicates.add(cb.equal(francesinha.get("type"), type));
                }

                // Solo se hace el JOIN a restaurant si se filtra por ciudad
                if (city != null && !city.isBlank()) {
                    Join<Object, Object> restaurant = francesinha.join("restaurant", JoinType.INNER);
                    predicates.add(cb.like(cb.lower(restaurant.get("city")),
                            "%" + city.toLowerCase() + "%"));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
