package com.ibisscore.user.service;

import com.ibisscore.common.exception.ResourceNotFoundException;
import com.ibisscore.user.dto.LeaderboardEntryDTO;
import com.ibisscore.user.dto.UserPredictionDTO;
import com.ibisscore.user.dto.UserPredictionRequest;
import com.ibisscore.user.entity.User;
import com.ibisscore.user.entity.UserPrediction;
import com.ibisscore.user.repository.UserPredictionRepository;
import com.ibisscore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserPredictionRepository predictionRepository;

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", id));
    }

    @Transactional
    public UserPredictionDTO submitPrediction(Long userId, UserPredictionRequest request) {
        User user = getById(userId);

        UserPrediction prediction = predictionRepository
                .findByUserIdAndFixtureId(userId, request.getFixtureId())
                .orElse(UserPrediction.builder()
                        .user(user)
                        .fixtureId(request.getFixtureId())
                        .build());

        prediction.setPredictedResult(request.getPredictedResult());
        prediction.setPredictedHome(request.getPredictedHome());
        prediction.setPredictedAway(request.getPredictedAway());

        UserPrediction saved = predictionRepository.save(prediction);
        return toDto(saved);
    }

    public List<UserPredictionDTO> getUserPredictions(Long userId) {
        return predictionRepository.findByUserId(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<LeaderboardEntryDTO> getLeaderboard(int limit) {
        return predictionRepository.findLeaderboard(PageRequest.of(0, limit))
                .stream()
                .map(row -> new LeaderboardEntryDTO(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue()
                ))
                .toList();
    }

    private UserPredictionDTO toDto(UserPrediction p) {
        UserPredictionDTO dto = new UserPredictionDTO();
        dto.setId(p.getId());
        dto.setFixtureId(p.getFixtureId());
        dto.setPredictedResult(p.getPredictedResult());
        dto.setPredictedHome(p.getPredictedHome());
        dto.setPredictedAway(p.getPredictedAway());
        dto.setPointsEarned(p.getPointsEarned());
        dto.setIsCorrect(p.getIsCorrect());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}
