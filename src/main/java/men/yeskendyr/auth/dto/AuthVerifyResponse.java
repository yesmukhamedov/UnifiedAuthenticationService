package men.yeskendyr.auth.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class AuthVerifyResponse {
    private UUID userId;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresInSeconds;
}
