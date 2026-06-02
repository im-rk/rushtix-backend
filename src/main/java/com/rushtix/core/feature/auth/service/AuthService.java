package com.rushtix.core.feature.auth.service;

import com.rushtix.core.domain.entities.User;
import com.rushtix.core.domain.enums.UserStatus;
import com.rushtix.core.feature.auth.dto.AuthResponse;
import com.rushtix.core.feature.auth.dto.SignupRequest;
import com.rushtix.core.feature.auth.repository.UserRepository;
import com.rushtix.core.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse registerUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("An account with this email already exists");
        }

        String securePasswordHash = passwordEncoder.encode(request.password());

        User newUser = User.builder()
                .email(request.email().toLowerCase().trim())
                .fullName(request.fullName().trim())
                .password_hash(securePasswordHash)
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(newUser);

        String token = jwtTokenProvider.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
    }
}
