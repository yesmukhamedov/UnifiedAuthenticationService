package men.yeskendyr.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otp")
public record OtpProperties(Duration ttl) {
}
