package men.yeskendyr.auth.dto;

import java.time.Instant;
import java.util.UUID;
import men.yeskendyr.auth.entity.IdentifierType;

public record OtpChallengeResponse(
        UUID challengeId,
        Instant expiresAt,
        String destination,
        IdentifierType type
) {
}
