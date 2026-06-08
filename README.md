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
- Bucket4j
- SpringDoc OpenAPI
- Docker & Docker Compose
- GitHub Actions

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
- Docker & Docker Compose (optional, for containerized run)

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

#### Using Gradle

Use the Gradle wrapper to run the application locally. To activate the `dev` profile (which enables dev database seeding):

- `./gradlew bootRun --args='--spring.profiles.active=dev'`

#### Using Docker Compose

Alternatively, you can run the application and its PostgreSQL database inside Docker containers:

- `docker compose up --build`

This will:
- Build the Spring Boot application image using the multi-stage `Dockerfile`
- Start a PostgreSQL 18.3 container (`keymanager-db`) exposed on port `5431`
- Start the application container (`keymanager-app`) exposed on port `8080`
- Activate the `dev` Spring profile
- Persist database records across restarts using a named Docker volume (`pgdata`)

### Dev Admin Account

When running the application with the `dev` profile active (such as when starting it via Docker Compose), a default administrator account is automatically seeded:

- **Email:** `admin@example.com`
- **Password:** `admin123`

You can use these credentials to authenticate via `POST /api/v1/login` to obtain an administrator JWT.

## Testing

The project includes comprehensive unit and integration tests covering:
- API key generation, validation, and revocation logic
- API key authentication filter, including malformed header handling and scope authorization
- AOP-based audit logging with SQL-based database cleanup for test isolation
- Global exception handling and JSON error response envelopes

Run the test suite with:

- `./gradlew test`

## Design Notes

- Passwords are stored with BCrypt hashing
- JWTs are signed and validated server-side
- API keys are stored as hashed secrets with a public prefix
- API key scopes are modeled as an element collection and fetched with an entity graph for authorization checks
- Audit logging is implemented using Aspect-Oriented Programming (AOP) and a custom `@Auditable` annotation to decouple logging from business logic
- Expired or revoked API keys are rejected by the authorization layer
- `ApiKeyAuthFilter` performs path-matching using the request URI and handles malformed keys with custom exception responses
- OpenAPI/Swagger endpoints bypass the API key filter completely
- Users are registered with a unique email constraint enforced at the database level
- Entities use the Lombok builder pattern with protected no-args constructors for JPA compatibility
- API key queries eagerly load scopes to avoid N+1 queries when listing keys
- API key list and retrieval endpoints include the granted scopes in the response metadata
- Custom exception handlers return JSON error envelopes instead of default HTML responses

## API Documentation (Swagger)

The API is fully documented using OpenAPI 3.0. When the application is running, you can access the interactive Swagger UI at:

`http://localhost:8080/swagger-ui.html`

The documentation includes:
- Detailed request/response schemas
- Security requirements for each endpoint (JWT vs API Key)
- Integrated "Try it out" functionality for all protected resources

## CI/CD Pipeline

A GitHub Actions workflow is implemented in `.github/workflows/ci.yml`. On every push to the `main` branch, the pipeline:
1. Spins up a PostgreSQL service container
2. Sets up a Java 21 environment
3. Executes the full Gradle test suite
4. Verifies the Docker image build
