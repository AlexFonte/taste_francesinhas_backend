package com.app.tastefrancesinhasbackend.repository;

import com.app.tastefrancesinhasbackend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByFrancesinhaId(Long francesinhaId);
    Optional<Review> findByFrancesinhaIdAndUserId(Long francesinhaId, Long userId);
    boolean existsByFrancesinhaIdAndUserId(Long francesinhaId, Long userId);
}
