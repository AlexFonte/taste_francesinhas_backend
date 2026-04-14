package com.app.tastefrancesinhasbackend.repository;

import com.app.tastefrancesinhasbackend.entity.Francesinha;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FrancesinhaRepository extends JpaRepository<Francesinha, Long>,
        JpaSpecificationExecutor<Francesinha> {

    // Carga restaurant y proposedBy en el mismo JOIN para evitar N+1 al serializar la lista
    @EntityGraph(attributePaths = {"restaurant", "proposedBy"})
    Page<Francesinha> findAll(Specification<Francesinha> spec, Pageable pageable);

    // Carga restaurant y proposedBy también en el detalle individual
    @EntityGraph(attributePaths = {"restaurant", "proposedBy"})
    Optional<Francesinha> findByIdAndStatus(Long id, FrancesinhaStatus status);

    // Igual que findByIdAndStatus pero sin filtrar por estado — para el detalle de admin
    @EntityGraph(attributePaths = {"restaurant", "proposedBy"})
    Optional<Francesinha> findById(Long id);

    // Recalcula avg_score sumando todos los avgScore de las reviews existentes (incluida la nueva).
    // Se ejecuta después de guardar la review, dentro del mismo @Transactional.
    @Modifying
    @Query(value = """
            UPDATE taste_francesinhas.francesinha
            SET avg_score = (
                SELECT SUM(r.avg_score) / COUNT(r.id)
                FROM taste_francesinhas.review r
                WHERE r.francesinha_id = :id
            ),
            total_reviews = total_reviews + 1
            WHERE id = :id
            """, nativeQuery = true)
    void updateScore(@Param("id") Long id);
}
