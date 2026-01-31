package men.yeskendyr.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import men.yeskendyr.auth.config.AuthProperties;
import men.yeskendyr.auth.config.JwtProperties;
import men.yeskendyr.auth.entity.RefreshToken;
import men.yeskendyr.auth.entity.User;
import men.yeskendyr.auth.exception.ApiException;
import men.yeskendyr.auth.exception.ErrorCode;
import men.yeskendyr.auth.repository.RefreshTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final AuthProperties authProperties;
    private final JwtKeyService jwtKeyService;
    private final Clock clock;

    public TokenService(RefreshTokenRepository refreshTokenRepository,
                        JwtProperties jwtProperties,
                        AuthProperties authProperties,
                        JwtKeyService jwtKeyService,
                        Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
        this.authProperties = authProperties;
        this.jwtKeyService = jwtKeyService;
        this.clock = clock;
        if (jwtProperties.getRefreshTokenSecret() == null || jwtProperties.getRefreshTokenSecret().isBlank()) {
            throw new IllegalStateException("auth.jwt.refresh-token-secret must be configured");
        }
    }

    public String issueAccessToken(User user) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(jwtProperties.getAccessTtl());
        var builder = Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("scope", "user")
                .claim("roles", List.of("USER"))
                .setId(UUID.randomUUID().toString())
                .setHeaderParam("kid", jwtKeyService.getKeyId())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .signWith(jwtKeyService.getPrivateKey(), SignatureAlgorithm.RS256);
        if (authProperties.getIssuer() != null && !authProperties.getIssuer().isBlank()) {
            builder.setIssuer(authProperties.getIssuer());
        }
        if (authProperties.getAudience() != null && !authProperties.getAudience().isEmpty()) {
            builder.claim("aud", authProperties.getAudience());
        }
        return builder.compact();
    }

    @Transactional
    public String issueRefreshToken(User user) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(jwtProperties.getRefreshTtl());
        String rawToken = generateOpaqueToken();
        String tokenHash = hashToken(rawToken);
        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiresAt, null, now);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public TokenPair issueTokens(User user) {
        String accessToken = issueAccessToken(user);
        String refreshToken = issueRefreshToken(user);
        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional
    public TokenPair refreshTokens(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED,
                        "Refresh token invalid"));
        Instant now = Instant.now(clock);
        if (existing.getExpiresAt().isBefore(now)) {
            existing.setRevokedAt(now);
            refreshTokenRepository.save(existing);
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Refresh token expired");
        }
        existing.setRevokedAt(now);
        refreshTokenRepository.save(existing);
        User user = existing.getUser();
        return issueTokens(user);
    }

    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .ifPresent(token -> {
                    token.setRevokedAt(Instant.now(clock));
                    refreshTokenRepository.save(token);
                });
    }

    public UUID parseUserId(String token) {
        try {
            String subject = parseClaims(token).getSubject();
            return UUID.fromString(subject);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid access token", ex);
        }
    }

    public Claims parseClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(jwtKeyService.getPublicKey())
                .build()
                .parseClaimsJws(token);
        Claims claims = jws.getBody();
        validateClaims(claims);
        return claims;
    }

    public List<String> extractRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof Collection<?> collection) {
            return collection.stream().map(Object::toString).toList();
        }
        return List.of("USER");
    }

    private void validateClaims(Claims claims) {
        String issuer = authProperties.getIssuer();
        if (issuer != null && !issuer.equals(claims.getIssuer())) {
            throw new JwtException("Invalid issuer");
        }
        List<String> expectedAudience = authProperties.getAudience();
        if (expectedAudience != null && !expectedAudience.isEmpty()) {
            Object audClaim = claims.get("aud");
            List<String> tokenAudience;
            if (audClaim instanceof Collection<?> collection) {
                tokenAudience = collection.stream().map(Object::toString).toList();
            } else if (audClaim != null) {
                tokenAudience = List.of(audClaim.toString());
            } else {
                tokenAudience = List.of();
            }
            boolean matches = tokenAudience.stream().anyMatch(expectedAudience::contains);
            if (!matches) {
                throw new JwtException("Invalid audience");
            }
        }
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String raw) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(jwtProperties.getRefreshTokenSecret()
                    .getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            mac.init(secretKey);
            byte[] digest = mac.doFinal(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to hash token", e);
        }
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;
    }
}
