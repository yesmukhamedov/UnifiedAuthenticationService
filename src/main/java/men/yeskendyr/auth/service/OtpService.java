package men.yeskendyr.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import men.yeskendyr.auth.config.OtpProperties;
import men.yeskendyr.auth.entity.IdentifierType;
import men.yeskendyr.auth.entity.OtpChallenge;
import men.yeskendyr.auth.entity.OtpPurpose;
import men.yeskendyr.auth.entity.User;
import men.yeskendyr.auth.exception.ApiException;
import men.yeskendyr.auth.exception.ErrorCode;
import men.yeskendyr.auth.repository.OtpChallengeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService {
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpChallengeRepository otpChallengeRepository;
    private final OtpProperties otpProperties;
    private final Clock clock;
    private final PasswordEncoder passwordEncoder;
    private final OtpGenerator otpGenerator;

    public OtpService(OtpChallengeRepository otpChallengeRepository,
                      OtpProperties otpProperties,
                      Clock clock,
                      PasswordEncoder passwordEncoder,
                      OtpGenerator otpGenerator) {
        this.otpChallengeRepository = otpChallengeRepository;
        this.otpProperties = otpProperties;
        this.clock = clock;
        this.passwordEncoder = passwordEncoder;
        this.otpGenerator = otpGenerator;
    }

    @Transactional
    public OtpChallenge createChallenge(OtpPurpose purpose, IdentifierType type, String value, User user) {
        String code = otpGenerator.generate();
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(otpProperties.ttl());
        String hash = passwordEncoder.encode(code);
        OtpChallenge challenge = new OtpChallenge(
                UUID.randomUUID(),
                purpose,
                type,
                value,
                user,
                hash,
                expiresAt,
                null,
                now
        );
        otpChallengeRepository.save(challenge);
        log.info("OTP for {} {}: {}", type, mask(type, value), code);
        return challenge;
    }

    @Transactional
    public OtpChallenge consumeChallenge(UUID challengeId, String code) {
        OtpChallenge challenge = otpChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "OTP challenge not found"));
        Instant now = Instant.now(clock);
        if (challenge.getConsumedAt() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.OTP_INVALID, "OTP already used");
        }
        if (challenge.getExpiresAt().isBefore(now)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.OTP_EXPIRED, "OTP expired");
        }
        if (!passwordEncoder.matches(code, challenge.getCodeHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.OTP_INVALID, "OTP invalid");
        }
        challenge.setConsumedAt(now);
        return otpChallengeRepository.save(challenge);
    }

    public String mask(IdentifierType type, String value) {
        if (value == null) {
            return "";
        }
        if (type == IdentifierType.EMAIL) {
            int at = value.indexOf('@');
            if (at <= 1) {
                return "***";
            }
            String local = value.substring(0, at);
            String domain = value.substring(at);
            String maskedLocal = local.substring(0, 1) + "***" + local.substring(local.length() - 1);
            return maskedLocal + domain;
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.length() <= 4) {
            return "***" + digits;
        }
        return "***" + digits.substring(digits.length() - 4);
    }
}
