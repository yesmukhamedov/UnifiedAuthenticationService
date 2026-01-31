package men.yeskendyr.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import men.yeskendyr.auth.dto.AuthStartRequest;
import men.yeskendyr.auth.dto.AuthVerifyRequest;
import men.yeskendyr.auth.dto.AuthVerifyResponse;
import men.yeskendyr.auth.dto.OtpChallengeResponse;
import men.yeskendyr.auth.service.OtpGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.util.LinkedMultiValueMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class WellKnownIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("auth_test")
            .withUsername("auth")
            .withPassword("auth");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("auth.issuer", () -> "http://localhost");
        registry.add("auth.audience", () -> "unified-services");
        registry.add("auth.introspection.client-id", () -> "test-client");
        registry.add("auth.introspection.client-secret", () -> "test-secret");
        registry.add("auth.jwt.access-ttl", () -> "PT15M");
        registry.add("auth.jwt.refresh-ttl", () -> "P7D");
        registry.add("auth.jwt.refresh-token-secret", () -> "test-refresh-secret-key");
        registry.add("otp.ttl", () -> "PT5M");
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @org.springframework.context.annotation.Primary
        MutableClock mutableClock() {
            return new MutableClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        }

        @Bean
        OtpGenerator otpGenerator() {
            return () -> "123456";
        }
    }

    private final TestRestTemplate restTemplate;

    WellKnownIntegrationTest(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @BeforeEach
    void setDefaults() {
        restTemplate.getRestTemplate().setInterceptors(List.of((request, body, execution) -> {
            request.getHeaders().setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));
            return execution.execute(request, body);
        }));
    }

    @Test
    void jwksAndIntrospectionWork() {
        ResponseEntity<Map> jwksResponse = restTemplate.getForEntity("/.well-known/jwks.json", Map.class);
        assertThat(jwksResponse.getStatusCode().is2xxSuccessful()).isTrue();
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwksResponse.getBody().get("keys");
        assertThat(keys).isNotEmpty();
        assertThat(keys.get(0).get("kid")).isNotNull();

        ResponseEntity<Map> oidcResponse = restTemplate.getForEntity("/.well-known/openid-configuration", Map.class);
        assertThat(oidcResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(oidcResponse.getBody().get("jwks_uri").toString()).contains("/.well-known/jwks.json");

        String accessToken = issueToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("test-client", "test-secret");
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("token", accessToken);
        ResponseEntity<Map> introspection = restTemplate.postForEntity(
                "/oauth2/introspect",
                new HttpEntity<>(body, headers),
                Map.class
        );
        assertThat(introspection.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(introspection.getBody().get("active")).isEqualTo(true);

        LinkedMultiValueMap<String, String> invalidBody = new LinkedMultiValueMap<>();
        invalidBody.add("token", "garbage");
        ResponseEntity<Map> invalidResponse = restTemplate.postForEntity(
                "/oauth2/introspect",
                new HttpEntity<>(invalidBody, headers),
                Map.class
        );
        assertThat(invalidResponse.getBody().get("active")).isEqualTo(false);
    }

    private String issueToken() {
        OtpChallengeResponse challenge = restTemplate.postForObject(
                "/api/v1/auth/start",
                new AuthStartRequest("user@example.com", null),
                OtpChallengeResponse.class
        );
        AuthVerifyResponse verifyResponse = restTemplate.postForObject(
                "/api/v1/auth/verify",
                new AuthVerifyRequest(challenge.getChallengeId(), "123456"),
                AuthVerifyResponse.class
        );
        return verifyResponse.getAccessToken();
    }
}
