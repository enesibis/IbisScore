package com.ibisscore.user.service;

import com.ibisscore.user.dto.AuthResponse;
import com.ibisscore.user.dto.LoginRequest;
import com.ibisscore.user.dto.RegisterRequest;
import com.ibisscore.user.entity.User;
import com.ibisscore.user.entity.UserRole;
import com.ibisscore.user.repository.UserRepository;
import com.ibisscore.user.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Bu kullanıcı adı zaten kullanılıyor: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kayıtlı: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getUsername());

        String token = jwtService.generateToken(saved);
        return buildAuthResponse(saved, token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı adı veya şifre hatalı"));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Bu hesap deaktive edilmiştir");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Kullanıcı adı veya şifre hatalı");
        }

        String token = jwtService.generateToken(user);
        log.info("User logged in: {}", user.getUsername());
        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
