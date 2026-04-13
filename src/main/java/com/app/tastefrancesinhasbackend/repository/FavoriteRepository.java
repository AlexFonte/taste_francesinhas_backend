package com.app.tastefrancesinhasbackend.repository;

import com.app.tastefrancesinhasbackend.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long>,
        JpaSpecificationExecutor<Favorite> {
    Optional<Favorite> findByUserIdAndFrancesinhaId(Long userId, Long francesinhaId);
}
