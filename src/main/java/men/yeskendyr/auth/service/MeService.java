package men.yeskendyr.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import men.yeskendyr.auth.dto.IdentifierResponse;
import men.yeskendyr.auth.dto.OtpChallengeResponse;
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
public class MeService {
    private final UserRepository userRepository;
    private final UserIdentifierRepository userIdentifierRepository;
    private final OtpService otpService;
    private final Clock clock;

    public MeService(UserRepository userRepository,
                     UserIdentifierRepository userIdentifierRepository,
                     OtpService otpService,
                     Clock clock) {
        this.userRepository = userRepository;
        this.userIdentifierRepository = userIdentifierRepository;
        this.otpService = otpService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<IdentifierResponse> listIdentifiers(UUID userId) {
        return userIdentifierRepository.findAllByUserId(userId).stream()
                .map(identifier -> new IdentifierResponse(identifier.getType(), identifier.getValue()))
                .toList();
    }

    @Transactional
    public OtpChallengeResponse startLink(UUID userId, IdentifierType type, String value) {
        if (userIdentifierRepository.existsByTypeAndValue(type, value)) {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCode.IDENTIFIER_IN_USE, "Identifier already in use");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "User not found"));
        OtpChallenge challenge = otpService.createChallenge(OtpPurpose.LINK, type, value, user);
        return new OtpChallengeResponse(challenge.getId(), challenge.getExpiresAt(), otpService.mask(type, value), type);
    }

    @Transactional
    public IdentifierResponse verifyLink(UUID userId, UUID challengeId, String code) {
        OtpChallenge challenge = otpService.consumeChallenge(challengeId, code);
        if (challenge.getPurpose() != OtpPurpose.LINK) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "Invalid OTP purpose");
        }
        if (challenge.getUser() == null || !challenge.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Unauthorized OTP challenge");
        }
        IdentifierType type = challenge.getType();
        String value = challenge.getValue();
        if (userIdentifierRepository.existsByTypeAndValue(type, value)) {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCode.IDENTIFIER_IN_USE, "Identifier already in use");
        }
        User user = challenge.getUser();
        Instant now = Instant.now(clock);
        UserIdentifier identifier = new UserIdentifier(user, type, value, true, now);
        userIdentifierRepository.save(identifier);
        return new IdentifierResponse(type, value);
    }
}
