package men.yeskendyr.auth.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
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
    private final Clock clock;
    private final SecretKey jwtKey;

    public TokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
        this.jwtKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueAccessToken(UUID userId) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(jwtProperties.getAccessTtl());
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(jwtKey, SignatureAlgorithm.HS256)
                .compact();
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
        String accessToken = issueAccessToken(user.getId());
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
            String subject = Jwts.parserBuilder()
                    .setSigningKey(jwtKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return UUID.fromString(subject);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid access token", ex);
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
            SecretKeySpec secretKey = new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8),
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
