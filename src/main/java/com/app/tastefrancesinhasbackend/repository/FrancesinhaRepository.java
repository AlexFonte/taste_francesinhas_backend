package com.app.tastefrancesinhasbackend.repository;

import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface FrancesinhaRepository extends JpaRepository<Francesinha, Long>,
        JpaSpecificationExecutor<Francesinha> {

    List<Francesinha> findByStatus(FrancesinhaStatus status);

    Optional<Francesinha> findByIdAndStatus(Long id, FrancesinhaStatus status);
}
