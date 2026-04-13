package com.app.tastefrancesinhasbackend.repository;

import com.app.tastefrancesinhasbackend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>,
        JpaSpecificationExecutor<Restaurant> {
}
