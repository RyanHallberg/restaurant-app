package com.ryanhallberg.restaurant.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryanhallberg.restaurant.common.error.ConflictException;
import com.ryanhallberg.restaurant.common.error.NotFoundException;
import com.ryanhallberg.restaurant.common.error.UnauthorizedException;
import com.ryanhallberg.restaurant.auth.dto.AuthResponse;
import com.ryanhallberg.restaurant.auth.dto.LoginRequest;
import com.ryanhallberg.restaurant.auth.dto.RegisterRequest;
import com.ryanhallberg.restaurant.auth.dto.UserResponse;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with that email already exists");
        }
        var user = userRepository.save(new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                Role.CUSTOMER));
        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.email())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                // Same message for unknown email and wrong password: don't leak
                // which emails have accounts.
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(long userId) {
        return userRepository.findById(userId)
                .map(AuthService::toResponse)
                .orElseThrow(() -> new NotFoundException("User %d not found".formatted(userId)));
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(tokenService.issue(user), TokenService.TTL.toSeconds(), toResponse(user));
    }

    private static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }
}
