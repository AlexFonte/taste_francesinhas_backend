package com.app.tastefrancesinhasbackend.entity;

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
@Table(name = "review", schema = "taste_francesinhas")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "francesinha_id", nullable = false)
    private Francesinha francesinha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "score_flavor", nullable = false, columnDefinition = "SMALLINT")
    @Builder.Default
    private Short scoreFlavor = 0;

    @Column(name = "score_sauce", nullable = false, columnDefinition = "SMALLINT")
    @Builder.Default
    private Short scoreSauce = 0;

    @Column(name = "score_bread", nullable = false, columnDefinition = "SMALLINT")
    @Builder.Default
    private Short scoreBread = 0;

    @Column(name = "score_presentation", nullable = false, columnDefinition = "SMALLINT")
    @Builder.Default
    private Short scorePresentation = 0;

    @Column(nullable = false, length = 500)
    private String comment;

    @Column(name = "avg_score", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal avgScore = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}