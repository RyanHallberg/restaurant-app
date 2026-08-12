package com.ryanhallberg.restaurant.auth.dto;

public record AuthResponse(String token, long expiresInSeconds, UserResponse user) {
}
