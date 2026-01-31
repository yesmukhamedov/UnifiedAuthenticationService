package men.yeskendyr.auth.config;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {
    private Duration accessTtl;
    private Duration refreshTtl;
    private String refreshTokenSecret;
    private String privateKeyPem;
    private String publicKeyPem;
    private String privateKeyLocation;
    private String publicKeyLocation;
}
