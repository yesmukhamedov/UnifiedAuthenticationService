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
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private Duration accessTtl;
    private Duration refreshTtl;
}
