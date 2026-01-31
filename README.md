# Unified Authentication Service

Passwordless authentication service built with Spring Boot 3.5, PostgreSQL, Flyway, JWT, and OpenFeign.

## Requirements

- JDK 17
- Maven 3.9+
- PostgreSQL 14+

## Configure

Update `src/main/resources/application.properties` or set environment variables:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `auth.issuer` (issuer for JWTs, e.g. `http://localhost:8080`)
- `auth.audience` (JWT audience, comma-separated)
- `auth.introspection.client-id`
- `auth.introspection.client-secret`
- `auth.jwt.access-ttl` (ISO-8601 duration, e.g. `PT15M`)
- `auth.jwt.refresh-ttl` (ISO-8601 duration, e.g. `P30D`)
- `auth.jwt.refresh-token-secret` (HMAC secret for refresh token hashing)
- `auth.jwt.private-key-pem` / `auth.jwt.public-key-pem` (optional PEM strings)
- `auth.jwt.private-key-location` / `auth.jwt.public-key-location` (optional resource locations)
- `otp.ttl` (ISO-8601 duration, e.g. `PT5M`)

## Local development

Flyway 11+ requires the `flyway-database-postgresql` module to recognize PostgreSQL 16+ database versions. Keep that dependency aligned with `flyway-core` so PostgreSQL 16.x starts cleanly without the "Unsupported Database" error.

```bash
docker compose down -v
docker compose up -d
```

```bash
cd /path/to/UnifiedAuthenticationService
docker compose up -d
mvn spring-boot:run
docker compose logs -f
docker compose down
```

Spring Boot uses the credentials from `application.properties`: `jdbc:postgresql://localhost:5432/unified_auth` with `auth_user` / `auth_password`.

## Sample flow

Start login with email:

```bash
curl -X POST http://localhost:8080/api/v1/auth/start \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'
```

Check the service logs for the OTP code, then verify:

```bash
curl -X POST http://localhost:8080/api/v1/auth/verify \
  -H "Content-Type: application/json" \
  -d '{"challengeId":"<uuid>","code":"123456"}'
```

Refresh tokens:

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<token>"}'
```

Get current user:

```bash
curl -X GET http://localhost:8080/api/v1/me \
  -H "Authorization: Bearer <accessToken>"
```

Link another identifier:

```bash
curl -X POST http://localhost:8080/api/v1/me/identifiers/start \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+77001234567"}'
```

Then verify it:

```bash
curl -X POST http://localhost:8080/api/v1/me/identifiers/verify \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"challengeId":"<uuid>","code":"123456"}'
```

## Client integration

Client services should validate access tokens locally by fetching the JWKS from the auth service and verifying JWTs with RS256.

Well-known endpoints:

```bash
curl http://localhost:8080/.well-known/jwks.json
curl http://localhost:8080/.well-known/openid-configuration
```

Service-to-service introspection (HTTP Basic auth):

```bash
curl -X POST http://localhost:8080/oauth2/introspect \
  -u service-client:service-secret \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=<accessToken>"
```

Example `application.properties` for auth-service:

```properties
auth.issuer=http://localhost:8080
auth.audience=unified-services
auth.base-url=http://localhost:8080
auth.introspection.client-id=service-client
auth.introspection.client-secret=service-secret
auth.jwt.access-ttl=PT15M
auth.jwt.refresh-ttl=P30D
auth.jwt.refresh-token-secret=change-me
# For stable keys:
# auth.jwt.private-key-location=classpath:keys/private.pem
# auth.jwt.public-key-location=classpath:keys/public.pem
# or set AUTH_JWT_PRIVATE_KEY_PEM / AUTH_JWT_PUBLIC_KEY_PEM env vars.
```
