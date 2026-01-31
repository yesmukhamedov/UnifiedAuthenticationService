package men.yeskendyr.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.ZoneOffset;
import men.yeskendyr.auth.entity.IdentifierType;
import men.yeskendyr.auth.entity.OtpPurpose;
import men.yeskendyr.auth.exception.ApiException;
import men.yeskendyr.auth.service.OtpGenerator;
import men.yeskendyr.auth.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class OtpServiceTest {
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

    private final OtpService otpService;
    private final MutableClock mutableClock;

    OtpServiceTest(OtpService otpService, MutableClock mutableClock) {
        this.otpService = otpService;
        this.mutableClock = mutableClock;
    }

    @BeforeEach
    void resetClock() {
        mutableClock.setInstant(Instant.parse("2025-01-01T00:00:00Z"));
    }

    @Test
    void otpIsOneTimeUse() {
        var challenge = otpService.createChallenge(OtpPurpose.LOGIN, IdentifierType.EMAIL, "user@example.com", null);
        otpService.consumeChallenge(challenge.getId(), "123456");
        assertThatThrownBy(() -> otpService.consumeChallenge(challenge.getId(), "123456"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void otpExpires() {
        var challenge = otpService.createChallenge(OtpPurpose.LOGIN, IdentifierType.EMAIL, "user@example.com", null);
        mutableClock.setInstant(Instant.parse("2025-01-01T00:10:00Z"));
        assertThatThrownBy(() -> otpService.consumeChallenge(challenge.getId(), "123456"))
                .isInstanceOf(ApiException.class);
    }
}
