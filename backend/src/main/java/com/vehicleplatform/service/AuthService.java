package com.vehicleplatform.service;

import com.vehicleplatform.dto.auth.TokenResponse;
import com.vehicleplatform.dto.auth.UserResponse;
import com.vehicleplatform.entity.User;
import com.vehicleplatform.exception.ApiException;
import com.vehicleplatform.repository.UserRepository;
import com.vehicleplatform.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse register(String email, String fullName, String password) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("Un compte existe déjà avec cet email.");
        }
        User user = User.builder()
                .email(email.toLowerCase())
                .fullName(fullName)
                .hashedPassword(passwordEncoder.encode(password))
                .active(true)
                .build();
        // saveAndFlush (not save): @CreationTimestamp/@UpdateTimestamp are populated
        // by Hibernate at flush time, which otherwise wouldn't happen until this
        // @Transactional method returns — too late to appear in the response below.
        user = userRepository.saveAndFlush(user);
        String token = jwtService.generateToken(user.getId());
        return TokenResponse.of(token, UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ApiException.unauthorized("Email ou mot de passe incorrect."));
        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw ApiException.unauthorized("Email ou mot de passe incorrect.");
        }
        if (!user.isActive()) {
            throw ApiException.unauthorized("Ce compte a été désactivé.");
        }
        String token = jwtService.generateToken(user.getId());
        return TokenResponse.of(token, UserResponse.from(user));
    }

    @Transactional
    public UserResponse updateProfile(User user, String fullName, String password) {
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if (password != null && !password.isBlank()) {
            user.setHashedPassword(passwordEncoder.encode(password));
        }
        return UserResponse.from(userRepository.saveAndFlush(user));
    }
}
