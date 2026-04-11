package com.app.tastefrancesinhasbackend.entity;

import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaStatus;
import com.app.tastefrancesinhasbackend.entity.enums.FrancesinhaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "francesinha", schema = "taste_francesinhas")
public class Francesinha {

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
    private Long totalReviews = 0l;

    @Column(name = "avg_score", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal avgScore = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}