package men.yeskendyr.auth.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private String issuer;
    private String baseUrl;
    private java.util.List<String> audience;
    private IntrospectionProperties introspection = new IntrospectionProperties();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntrospectionProperties {
        private String clientId;
        private String clientSecret;
    }
}
