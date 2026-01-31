package men.yeskendyr.auth.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import men.yeskendyr.auth.entity.IdentifierType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class OtpChallengeResponse {
    private UUID challengeId;
    private Instant expiresAt;
    private String destination;
    private IdentifierType type;
}
