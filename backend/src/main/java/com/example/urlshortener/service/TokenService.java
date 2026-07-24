package com.example.urlshortener.service;

import com.example.urlshortener.config.JwtConfig;
import com.example.urlshortener.config.JwtProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class TokenService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public TokenService(JwtEncoder jwtEncoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    /** Claims are readable by anyone holding the token: a JWT is signed, not encrypted. */
    public AccessToken mintAccessToken(Authentication authentication) {
        Instant issuedAt = Instant.now();
        Duration ttl = properties.getAccessTokenTtl();
        Instant expiresAt = issuedAt.plus(ttl);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith(ROLE_PREFIX)
                        ? authority.substring(ROLE_PREFIX.length())
                        : authority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuer(properties.getIssuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(JwtConfig.ROLES_CLAIM, roles)
                .build();

        // Required: the encoder otherwise defaults to RS256, which does not match
        // the HMAC signing key.
        JwsHeader header = JwsHeader.with(JwtConfig.SIGNING_ALGORITHM).build();

        String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new AccessToken(value, expiresAt, ttl.toSeconds());
    }

    public record AccessToken(String value, Instant expiresAt, long expiresInSeconds) {
    }
}
