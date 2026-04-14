package com.app.tastefrancesinhasbackend.repository;

import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface FrancesinhaRepository extends JpaRepository<Francesinha, Long>,
        JpaSpecificationExecutor<Francesinha> {

    // Carga restaurant y proposedBy en el mismo JOIN para evitar N+1 al serializar la lista
    @EntityGraph(attributePaths = {"restaurant", "proposedBy"})
    Page<Francesinha> findAll(Specification<Francesinha> spec, Pageable pageable);

    List<Francesinha> findByStatus(FrancesinhaStatus status);
    
    // Carga restaurant y proposedBy también en el detalle individual
    @EntityGraph(attributePaths = {"restaurant", "proposedBy"})
    Optional<Francesinha> findByIdAndStatus(Long id, FrancesinhaStatus status);
}
