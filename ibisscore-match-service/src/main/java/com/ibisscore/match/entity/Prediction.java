package com.ibisscore.match.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "predictions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"fixture_id", "model_version"}))
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @Column(nullable = false, length = 20)
    private String modelVersion;

    // 1X2 olasılıkları
    private Double homeWinProb;
    private Double drawProb;
    private Double awayWinProb;

    // Beklenen goller
    private Double predictedHomeGoals;
    private Double predictedAwayGoals;

    // Ek marketler
    private Double over25Prob;
    private Double bttsProb;

    // Güven skoru
    private Double confidenceScore;

    // Öneri
    @Column(length = 20)
    private String recommendation;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
