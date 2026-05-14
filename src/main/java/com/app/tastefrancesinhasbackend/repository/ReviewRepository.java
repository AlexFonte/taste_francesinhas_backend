package com.app.tastefrancesinhasbackend.repository;

import com.app.tastefrancesinhasbackend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByFrancesinhaId(Long francesinhaId, Pageable pageable);

    Optional<Review> findByFrancesinhaIdAndUserId(Long francesinhaId, Long userId);

    long countByUserId(Long userId);

    // Reviews del usuario con francesinha + restaurante cargados en el mismo JOIN
    @EntityGraph(attributePaths = {"francesinha", "francesinha.restaurant"})
    Page<Review> findByUserId(Long userId, Pageable pageable);

    List<Review> findByUserIdAndFrancesinhaIdInOrderByCreatedAtAsc(Long userId, Collection<Long> francesinhaIds);
}
