# KeyManager

KeyManager is a Spring Boot backend for managing users, JWT-based authentication, and scoped API key issuance for protected machine-to-machine data access.

It was built as a backend training project, but the implementation follows production-style patterns: stateless auth, password hashing, role-based authorization, hashed API key storage, and structured error handling.

## What It Does

- Creates users with encrypted passwords
- Authenticates users with JWT access tokens
- Issues, lists, and revokes scoped API keys
- Records audit logs for security-sensitive actions (API key creation/revocation)
- Protects user management and audit log endpoints with JWT role-based access control
- Protects sample data endpoints with API-key scope checks
- Validates API keys through a dedicated request filter
- Returns consistent JSON error responses for auth and access failures

## Architecture

The application is organized by feature area:

- `user` handles registration, login, and user lookup
- `apikey` handles API key generation, scope validation, listing, revocation, and request filtering
- `audit` handles automatic action logging via AOP and log retrieval API
- `config` contains Spring Security and JWT filter wiring
- `exception` centralizes domain and API error handling
- `data` exposes a protected sample endpoint used to demonstrate authorization flows

Security is fully stateless:

- Users log in with email and password
- The server issues a signed JWT
- Requests include the JWT in the `Authorization: Bearer ...` header
- User and API-key management requests use JWT authentication
- Machine-to-machine data requests use the `x-api-key` header
- API key secrets are stored hashed in the database, not in plain text
- API key scopes are stored separately from the key secret and are loaded for authorization checks

## API Key Scopes

API keys support these scopes:

- `READ` allows `GET /api/v1/data`
- `WRITE` allows `POST /api/v1/data`
- `ADMIN` allows all scoped data operations

The authenticated user creating a key controls which scopes are requested:

- Regular users may create non-admin scoped keys for themselves
- Admin users may create keys containing `ADMIN`
- Empty or missing scope sets are rejected

`ADMIN` is treated as an override during authorization. A key with `ADMIN` passes checks for `READ` and `WRITE`.

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- JWT (`jjwt`)
- JUnit 5 + Mockito

## Core Endpoints

| Method  | Endpoint | Access | Purpose |
|---| --- | --- | --- |
| `POST`  | `/api/v1/users` | Public | Create a new user |
| `GET`   | `/api/v1/users` | ADMIN only | List users |
| `GET`   | `/api/v1/audit-logs` | ADMIN only | Retrieve latest system audit logs |
| `POST`  | `/api/v1/login` | Public | Exchange credentials for a JWT |
| `POST`  | `/api/v1/users/{userId}/apikeys` | Owner of {userId} or ADMIN | Generate an API key |
| `GET`   | `/api/v1/users/{userId}/apikeys` | Owner of {userId} or ADMIN | List API keys for a user |
| `DELETE` | `/api/v1/users/{userId}/apikeys/{apiKeyId}` | Owner of {userId} or ADMIN | Revoke an API key |
| `GET`   | `/api/v1/data` | API key with READ or ADMIN | Example machine-to-machine read endpoint |
| `POST`  | `/api/v1/data` | API key with WRITE or ADMIN | Example machine-to-machine write endpoint |
| `DELETE` | `/api/v1/data` | API key with ADMIN | Example machine-to-machine admin/delete endpoint |

API key endpoints use ownership-based authorization: regular users can only create, list, and revoke keys for their
own userId, while admins can manage any user’s keys.

The `/api/v1/data` endpoint is configured as `permitAll()` in Spring Security because it is authenticated by the
custom `ApiKeyAuthFilter`, not by JWT. The filter applies to `/api/v1/data` and requires a valid `x-api-key`
header with the correct scope for the HTTP method.

## Request Flow

1. Register a user through `/api/v1/users`
2. Log in through `/api/v1/login`
3. Use the JWT for authenticated requests
4. Generate an API key for the target user with one or more scopes
5. Send the API key in `x-api-key` when calling `/api/v1/data`
6. The API-key filter maps the HTTP method to a required scope and checks the stored key scopes

Example API key creation body:

```json
{
  "scopes": ["READ", "WRITE"]
}
```

The raw API key is returned only at creation time. After that, only the hashed secret is stored.

## Local Setup

### Prerequisites

- Java 21
- PostgreSQL
- Gradle Wrapper included in the repo

### Database

The default configuration expects PostgreSQL to be available at:

- `jdbc:postgresql://localhost:5431/keymanagerdb`
- username: `postgres`
- password: `mysecretpass`

You can change these values in `src/main/resources/application.properties`.

### Environment Variables

The app reads these optional environment variables:

- `JWT_SECRET` for the signing key
- `JWT_EXPIRATION_MS` for JWT lifetime in milliseconds

If they are not set, the application falls back to development defaults from `application.properties`.

### Run the Application

Use the Gradle wrapper:

- `./gradlew bootRun`

## Testing

The project includes unit tests for API key generation and a Spring Boot application context test.

Run the test suite with:

- `./gradlew test`

## Design Notes

- Passwords are stored with BCrypt hashing
- JWTs are signed and validated server-side
- API keys are stored as hashed secrets with a public prefix
- API key scopes are modeled as an element collection and fetched with an entity graph for authorization checks
- Audit logging is implemented using Aspect-Oriented Programming (AOP) and a custom `@Auditable` annotation to decouple logging from business logic
- Expired or revoked API keys are rejected by the authorization layer
- `ApiKeyAuthFilter` maps `GET` to `READ`, `POST` to `WRITE`, and `DELETE` to `ADMIN`
- Custom exception handlers return JSON error envelopes instead of default HTML responses
