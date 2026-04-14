package com.app.tastefrancesinhasbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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
    private Short scoreFlavor = 1;

    @Column(name = "score_sauce", nullable = false, columnDefinition = "SMALLINT")
    @Builder.Default
    private Short scoreSauce = 1;

    @Column(name = "score_bread", nullable = false, columnDefinition = "SMALLINT")
    @Builder.Default
    private Short scoreBread = 1;

    @Column(name = "score_presentation", nullable = false, columnDefinition = "SMALLINT")
    @Builder.Default
    private Short scorePresentation = 1;

    @Column(nullable = false, length = 500)
    private String comment;

    @Column(name = "avg_score", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal avgScore = BigDecimal.ONE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}