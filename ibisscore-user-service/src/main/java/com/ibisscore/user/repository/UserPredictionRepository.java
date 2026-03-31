package com.ibisscore.user.repository;

import com.ibisscore.user.entity.UserPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPredictionRepository extends JpaRepository<UserPrediction, Long> {

    List<UserPrediction> findByUserId(Long userId);

    Optional<UserPrediction> findByUserIdAndFixtureId(Long userId, Long fixtureId);

    @Query("""
            SELECT u.id, u.username,
                   COALESCE(SUM(up.pointsEarned), 0)     AS totalPoints,
                   COUNT(CASE WHEN up.isCorrect = true THEN 1 END) AS correctPredictions,
                   COUNT(up.id)                           AS totalPredictions
            FROM UserPrediction up
            JOIN up.user u
            GROUP BY u.id, u.username
            ORDER BY totalPoints DESC
            LIMIT :limit
            """)
    List<Object[]> findLeaderboard(int limit);
}
