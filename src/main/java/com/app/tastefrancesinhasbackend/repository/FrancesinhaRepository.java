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

    // Igual que findByIdAndStatus pero sin filtrar por estado - para el detalle de admin
    @EntityGraph(attributePaths = {"restaurant", "proposedBy"})
    Optional<Francesinha> findById(Long id);

    // Cuenta cuantas francesinhas hay en un estado concreto. Lo usa el dashboard de admin
    // para mostrar los contadores (pendientes, aprobadas, rechazadas).
    long countByStatus(FrancesinhaStatus status);

    // Recalcula avg_score y las 4 medias por criterio sumando todas las reviews existentes (incluida la nueva).
    // Se ejecuta despues de guardar la review, dentro del mismo @Transactional. 
    // COALESCE para que cuand no hay reviews la columna NOT NULL no falle.
    @Modifying
    @Query(value = """
            UPDATE taste_francesinhas.francesinha
            SET avg_score          = COALESCE((SELECT AVG(r.avg_score)          FROM taste_francesinhas.review r WHERE r.francesinha_id = :id), 0),
                avg_flavor         = COALESCE((SELECT AVG(r.score_flavor)       FROM taste_francesinhas.review r WHERE r.francesinha_id = :id), 0),
                avg_sauce          = COALESCE((SELECT AVG(r.score_sauce)        FROM taste_francesinhas.review r WHERE r.francesinha_id = :id), 0),
                avg_bread          = COALESCE((SELECT AVG(r.score_bread)        FROM taste_francesinhas.review r WHERE r.francesinha_id = :id), 0),
                avg_presentation   = COALESCE((SELECT AVG(r.score_presentation) FROM taste_francesinhas.review r WHERE r.francesinha_id = :id), 0),
                total_reviews      = (SELECT COUNT(r.id) FROM taste_francesinhas.review r WHERE r.francesinha_id = :id)
            WHERE id = :id
            """, nativeQuery = true)
    void updateScore(@Param("id") Long id);
}
