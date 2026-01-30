package men.yeskendyr.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneOffset;
import men.yeskendyr.auth.dto.AuthStartRequest;
import men.yeskendyr.auth.dto.AuthVerifyRequest;
import men.yeskendyr.auth.dto.AuthVerifyResponse;
import men.yeskendyr.auth.dto.MeResponse;
import men.yeskendyr.auth.dto.OtpChallengeResponse;
import men.yeskendyr.auth.dto.RefreshRequest;
import men.yeskendyr.auth.dto.TokenResponse;
import men.yeskendyr.auth.service.OtpGenerator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class AuthFlowIntegrationTest {
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
        registry.add("jwt.secret", () -> "test-secret-key-test-secret-key-test-secret-key");
        registry.add("jwt.access-ttl", () -> "PT15M");
        registry.add("jwt.refresh-ttl", () -> "P7D");
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

    AuthFlowIntegrationTest(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @BeforeEach
    void setDefaults() {
        restTemplate.getRestTemplate().setInterceptors(List.of((request, body, execution) -> {
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            request.getHeaders().setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));
            return execution.execute(request, body);
        }));
    }

    @Test
    void authFlowWorks() {
        OtpChallengeResponse challenge = restTemplate.postForObject(
                "/api/v1/auth/start",
                new AuthStartRequest("user@example.com", null),
                OtpChallengeResponse.class
        );

        assertThat(challenge).isNotNull();
        AuthVerifyResponse verifyResponse = restTemplate.postForObject(
                "/api/v1/auth/verify",
                new AuthVerifyRequest(challenge.challengeId(), "123456"),
                AuthVerifyResponse.class
        );
        assertThat(verifyResponse).isNotNull();
        assertThat(verifyResponse.accessToken()).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(verifyResponse.accessToken());
        ResponseEntity<MeResponse> meResponse = restTemplate.exchange(
                "/api/v1/me",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                MeResponse.class
        );
        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(meResponse.getBody()).isNotNull();
        assertThat(meResponse.getBody().identifiers()).isNotEmpty();

        TokenResponse refresh = restTemplate.postForObject(
                "/api/v1/auth/refresh",
                new RefreshRequest(verifyResponse.refreshToken()),
                TokenResponse.class
        );
        assertThat(refresh).isNotNull();
        assertThat(refresh.accessToken()).isNotBlank();
    }
}
