package com.ibisscore.user.controller;

import com.ibisscore.common.dto.ApiResponse;
import com.ibisscore.user.dto.LeaderboardEntryDTO;
import com.ibisscore.user.dto.UserPredictionDTO;
import com.ibisscore.user.dto.UserPredictionRequest;
import com.ibisscore.user.entity.User;
import com.ibisscore.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users/me — kimlik doğrulanmış kullanıcı profili.
     * Gateway X-User-Id header'ını ekler; servis oradan okur.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(
            @RequestHeader("X-User-Id") Long userId) {
        User user = userService.getById(userId);
        Map<String, Object> profile = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "createdAt", user.getCreatedAt()
        );
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /** POST /api/users/predictions — tahmin gönder */
    @PostMapping("/predictions")
    public ResponseEntity<ApiResponse<UserPredictionDTO>> submitPrediction(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UserPredictionRequest request) {
        UserPredictionDTO dto = userService.submitPrediction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Tahmin kaydedildi"));
    }

    /** GET /api/users/predictions — kullanıcının geçmiş tahminleri */
    @GetMapping("/predictions")
    public ResponseEntity<ApiResponse<List<UserPredictionDTO>>> getMyPredictions(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserPredictions(userId)));
    }

    /** GET /api/users/leaderboard — top kullanıcı sıralaması */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDTO>>> getLeaderboard(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(userService.getLeaderboard(limit)));
    }
}
