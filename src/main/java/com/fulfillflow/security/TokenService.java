package com.fulfillflow.security;

import java.time.Duration;
import java.time.Instant;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
class TokenService {
    private static final Duration TOKEN_TTL = Duration.ofHours(1);
    private final JwtEncoder encoder;

    TokenService(JwtEncoder encoder) { this.encoder = encoder; }

    TokenResponse issue(Authentication authentication) {
        var now = Instant.now();
        var expiresAt = now.plus(TOKEN_TTL);
        var authorities = authentication.getAuthorities().stream().map(Object::toString).toList();
        var claims = JwtClaimsSet.builder()
                .issuer("fulfillflow")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(authentication.getName())
                .claim("authorities", authorities)
                .build();
        var token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expiresAt);
    }
}
