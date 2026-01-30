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
- `jwt.secret` (must be a long random string)
- `jwt.access-ttl` (ISO-8601 duration, e.g. `PT15M`)
- `jwt.refresh-ttl` (ISO-8601 duration, e.g. `P30D`)
- `otp.ttl` (ISO-8601 duration, e.g. `PT5M`)

## Run PostgreSQL (Docker)

```bash
docker run --name unified-auth-db -e POSTGRES_DB=unified_auth \
  -e POSTGRES_USER=auth_user -e POSTGRES_PASSWORD=auth_password \
  -p 5432:5432 -d postgres:16
```

## Run the service

```bash
./mvnw spring-boot:run
```

Flyway migrations are applied on startup.

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
