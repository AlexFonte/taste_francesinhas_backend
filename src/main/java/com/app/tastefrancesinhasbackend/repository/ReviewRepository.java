package com.app.tastefrancesinhasbackend.repository;

import com.app.tastefrancesinhasbackend.entity.Review;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByFrancesinhaId(Long francesinhaId, Pageable pageable);

    Optional<Review> findByFrancesinhaIdAndUserId(Long francesinhaId, Long userId);

    long countByUserId(Long userId);

    // Reviews del usuario con francesinha + restaurante cargados en el mismo JOIN.
    // Filtramos por estado de la francesinha: en el perfil solo se listan las reviews
    // de francesinhas ACCEPTED, las de propuestas pendientes o rechazadas no se muestran.
    @EntityGraph(attributePaths = {"francesinha", "francesinha.restaurant"})
    Page<Review> findByUserIdAndFrancesinhaStatus(Long userId, FrancesinhaStatus status, Pageable pageable);

    List<Review> findByUserIdAndFrancesinhaIdInOrderByCreatedAtAsc(Long userId, Collection<Long> francesinhaIds);

    // Para cada francesinha en la lista de ids devuelve la foto de la review mas reciente
    // que tenga foto. Usa ROW_NUMBER() para coger solo la primera ordenada por fecha DESC.
    // Devuelve [francesinha_id, photo_url] como Object[] que el service mapea a Map<Long, String>.
    @Query(value = """
            SELECT francesinha_id, photo_url FROM (
                SELECT r.francesinha_id, r.photo_url,
                       ROW_NUMBER() OVER (PARTITION BY r.francesinha_id ORDER BY r.created_at DESC) AS rn
                FROM taste_francesinhas.review r
                WHERE r.francesinha_id IN (:ids) AND r.photo_url IS NOT NULL
            ) sub WHERE sub.rn = 1
            """, nativeQuery = true)
    List<Object[]> findCoverPhotoUrlsByFrancesinhaIds(@Param("ids") Collection<Long> ids);

    // Todas las URLs de las fotos de las reviews de una francesinha, ordenadas por fecha DESC
    // (la mas reciente primero). Para alimentar el carrusel del detalle sin depender de
    // la paginacion de reviews.
    @Query("""
            SELECT r.photoUrl FROM Review r
            WHERE r.francesinha.id = :francesinhaId AND r.photoUrl IS NOT NULL
            ORDER BY r.createdAt DESC
            """)
    List<String> findPhotoUrlsByFrancesinhaId(@Param("francesinhaId") Long francesinhaId);
}
