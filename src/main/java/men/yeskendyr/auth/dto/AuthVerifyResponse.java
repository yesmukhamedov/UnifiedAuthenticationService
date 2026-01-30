package men.yeskendyr.auth.dto;

import java.util.UUID;

public record AuthVerifyResponse(
        UUID userId,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds
) {
}
