package com.app.tastefrancesinhasbackend.repository;

import com.app.tastefrancesinhasbackend.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long>,
        JpaSpecificationExecutor<Favorite> {

    // Carga francesinha y su restaurant en el mismo JOIN para evitar N+1 al serializar los favoritos
    @EntityGraph(attributePaths = {"francesinha", "francesinha.restaurant"})
    Page<Favorite> findAll(Specification<Favorite> spec, Pageable pageable);

    Optional<Favorite> findByUserIdAndFrancesinhaId(Long userId, Long francesinhaId);
}
