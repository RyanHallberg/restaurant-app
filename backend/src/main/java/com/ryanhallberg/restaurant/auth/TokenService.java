package com.ryanhallberg.restaurant.auth;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    static final Duration TTL = Duration.ofMinutes(60);

    private final JwtEncoder jwtEncoder;
    private final Clock clock;

    TokenService(JwtEncoder jwtEncoder, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
    }

    String issue(User user) {
        var now = clock.instant();
        var claims = JwtClaimsSet.builder()
                .issuer("restaurant-api")
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(now.plus(TTL))
                .claim("email", user.getEmail())
                .claim("roles", List.of(user.getRole().name()))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
