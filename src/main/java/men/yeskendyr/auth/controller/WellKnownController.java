package men.yeskendyr.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import men.yeskendyr.auth.config.AuthProperties;
import men.yeskendyr.auth.service.JwksService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class WellKnownController {
    private final AuthProperties authProperties;
    private final JwksService jwksService;

    public WellKnownController(AuthProperties authProperties, JwksService jwksService) {
        this.authProperties = authProperties;
        this.jwksService = jwksService;
    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> jwks() {
        return ResponseEntity.ok(jwksService.getJwks());
    }

    @GetMapping(value = "/.well-known/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> openIdConfiguration(HttpServletRequest request) {
        String baseUrl = resolveBaseUrl(request);
        Map<String, Object> response = Map.of(
                "issuer", authProperties.getIssuer(),
                "jwks_uri", baseUrl + "/.well-known/jwks.json",
                "introspection_endpoint", baseUrl + "/oauth2/introspect",
                "token_endpoint", baseUrl + "/api/v1/auth/verify",
                "response_types_supported", List.of("token"),
                "subject_types_supported", List.of("public"),
                "id_token_signing_alg_values_supported", List.of("RS256"),
                "scopes_supported", List.of("user"),
                "claims_supported", List.of("sub", "iss", "aud", "exp", "iat", "scope", "roles", "jti")
        );
        return ResponseEntity.ok(response);
    }

    private String resolveBaseUrl(HttpServletRequest request) {
        if (authProperties.getBaseUrl() != null && !authProperties.getBaseUrl().isBlank()) {
            return authProperties.getBaseUrl().replaceAll("/$", "");
        }
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("")
                .build()
                .toUriString();
    }
}
