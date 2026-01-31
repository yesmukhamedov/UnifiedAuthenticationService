package men.yeskendyr.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import men.yeskendyr.auth.dto.AuthStartRequest;
import men.yeskendyr.auth.dto.AuthVerifyRequest;
import men.yeskendyr.auth.dto.AuthVerifyResponse;
import men.yeskendyr.auth.dto.OtpChallengeResponse;
import men.yeskendyr.auth.dto.TokenResponse;
import men.yeskendyr.auth.config.JwtProperties;
import men.yeskendyr.auth.entity.IdentifierType;
import men.yeskendyr.auth.entity.OtpChallenge;
import men.yeskendyr.auth.entity.OtpPurpose;
import men.yeskendyr.auth.entity.User;
import men.yeskendyr.auth.entity.UserIdentifier;
import men.yeskendyr.auth.exception.ApiException;
import men.yeskendyr.auth.exception.ErrorCode;
import men.yeskendyr.auth.repository.UserIdentifierRepository;
import men.yeskendyr.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final OtpService otpService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final UserIdentifierRepository userIdentifierRepository;
    private final Clock clock;
    private final JwtProperties jwtProperties;

    public AuthService(OtpService otpService,
                       TokenService tokenService,
                       UserRepository userRepository,
                       UserIdentifierRepository userIdentifierRepository,
                       Clock clock,
                       JwtProperties jwtProperties) {
        this.otpService = otpService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.userIdentifierRepository = userIdentifierRepository;
        this.clock = clock;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public OtpChallengeResponse start(AuthStartRequest request) {
        IdentifierType type = request.getEmail() != null ? IdentifierType.EMAIL : IdentifierType.PHONE;
        String value = IdentifierNormalizer.normalize(type,
                request.getEmail() != null ? request.getEmail() : request.getPhoneNumber());
        OtpChallenge challenge = otpService.createChallenge(OtpPurpose.LOGIN, type, value, null);
        return new OtpChallengeResponse(challenge.getId(), challenge.getExpiresAt(),
                otpService.mask(type, value), type);
    }

    @Transactional
    public AuthVerifyResponse verify(AuthVerifyRequest request) {
        OtpChallenge challenge = otpService.consumeChallenge(request.getChallengeId(), request.getCode());
        if (challenge.getPurpose() != OtpPurpose.LOGIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "Invalid OTP purpose");
        }
        IdentifierType type = challenge.getType();
        String value = challenge.getValue();
        User user = userIdentifierRepository.findByTypeAndValue(type, value)
                .map(UserIdentifier::getUser)
                .orElseGet(() -> createUserWithIdentifier(type, value));
        TokenService.TokenPair tokens = tokenService.issueTokens(user);
        return new AuthVerifyResponse(user.getId(), tokens.getAccessToken(), tokens.getRefreshToken(),
                "Bearer", jwtProperties.getAccessTtl().toSeconds());
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        TokenService.TokenPair tokens = tokenService.refreshTokens(refreshToken);
        return new TokenResponse(tokens.getAccessToken(), tokens.getRefreshToken(), "Bearer",
                jwtProperties.getAccessTtl().toSeconds());
    }

    @Transactional
    public void logout(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
    }

    private User createUserWithIdentifier(IdentifierType type, String value) {
        Instant now = Instant.now(clock);
        User user = new User(UUID.randomUUID(), now);
        userRepository.save(user);
        UserIdentifier identifier = new UserIdentifier(user, type, value, true, now);
        userIdentifierRepository.save(identifier);
        return user;
    }

}
