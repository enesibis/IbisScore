package com.ibisscore.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_predictions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "fixture_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fixture_id", nullable = false)
    private Long fixtureId;

    @Column(name = "predicted_result", length = 10)
    private String predictedResult;  // "1", "X", "2"

    @Column(name = "predicted_home")
    private Integer predictedHome;

    @Column(name = "predicted_away")
    private Integer predictedAway;

    @Column(name = "points_earned")
    @Builder.Default
    private Integer pointsEarned = 0;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
