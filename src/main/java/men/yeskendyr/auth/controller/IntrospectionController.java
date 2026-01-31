package men.yeskendyr.auth.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import men.yeskendyr.auth.service.TokenService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IntrospectionController {
    private final TokenService tokenService;

    public IntrospectionController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping(value = "/oauth2/introspect", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> introspect(@RequestParam("token") String token) {
        try {
            Claims claims = tokenService.parseClaims(token);
            Map<String, Object> response = Map.of(
                    "active", true,
                    "sub", claims.getSubject(),
                    "iss", claims.getIssuer(),
                    "aud", normalizeAudience(claims.get("aud")),
                    "exp", toEpochSeconds(claims.getExpiration().toInstant()),
                    "iat", toEpochSeconds(claims.getIssuedAt().toInstant()),
                    "scope", claims.get("scope"),
                    "roles", tokenService.extractRoles(claims),
                    "jti", claims.getId()
            );
            return ResponseEntity.ok(response);
        } catch (JwtException | IllegalArgumentException ex) {
            return ResponseEntity.ok(Map.of("active", false));
        }
    }

    private List<String> normalizeAudience(Object aud) {
        if (aud instanceof Collection<?> collection) {
            return collection.stream().map(Object::toString).toList();
        }
        if (aud != null) {
            return List.of(aud.toString());
        }
        return List.of();
    }

    private long toEpochSeconds(Instant instant) {
        return instant.getEpochSecond();
    }
}
