package com.app.tastefrancesinhasbackend.entity;

import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"restaurant", "proposedBy"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "francesinha", schema = "taste_francesinhas")
public class Francesinha {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposed_by", nullable = false)
    private User proposedBy;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "has_egg", nullable = false)
    @Builder.Default
    private boolean hasEgg = false;

    @Column(name = "has_fries", nullable = false)
    @Builder.Default
    private boolean hasFries = false;

    @Column(name = "is_spicy", nullable = false)
    @Builder.Default
    private boolean isSpicy = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrancesinhaType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FrancesinhaStatus status = FrancesinhaStatus.PENDING;

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Long totalReviews = 0L;

    @Column(name = "avg_score", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal avgScore = BigDecimal.ZERO;

    // Medias por criterio: se recalculan en updateScore() junto con avg_score, no se tocan desde Java.
    @Column(name = "avg_flavor", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal avgFlavor = BigDecimal.ZERO;

    @Column(name = "avg_sauce", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal avgSauce = BigDecimal.ZERO;

    @Column(name = "avg_bread", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal avgBread = BigDecimal.ZERO;

    @Column(name = "avg_presentation", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal avgPresentation = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}