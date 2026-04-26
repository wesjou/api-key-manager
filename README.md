# KeyManager

KeyManager is a Spring Boot backend for managing users, JWT-based authentication, and API key issuance for protected access to internal data endpoints.

It was built as a backend training project, but the implementation follows production-style patterns: stateless auth, password hashing, role-based authorization, hashed API key storage, and structured error handling.

## What It Does

- Creates users with encrypted passwords
- Authenticates users with JWT access tokens
- Issues, lists, and revokes API keys
- Protects data endpoints with role-based access control
- Validates API keys through a dedicated request filter
- Returns consistent JSON error responses for auth and access failures

## Architecture

The application is organized by feature area:

- `user` handles registration, login, and user lookup
- `apikey` handles API key generation, validation, listing, and revocation
- `config` contains Spring Security and JWT filter wiring
- `exception` centralizes domain and API error handling
- `data` exposes a protected sample endpoint used to demonstrate authorization flows

Security is fully stateless:

- Users log in with email and password
- The server issues a signed JWT
- Requests include the JWT in the `Authorization: Bearer ...` header
- Protected data requests also require an `x-api-key` header
- API keys are stored hashed in the database, not in plain text

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

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/v1/users` | Public | Create a new user |
| `GET` | `/api/v1/users` | Admin only | List users |
| `POST` | `/api/v1/login` | Public | Exchange credentials for a JWT |
| `POST` | `/api/v1/users/{userId}/apikeys` | Owner of {userId} or ADMIN | Generate an API key |
| `GET` | `/api/v1/users/{userId}/apikeys` | Owner of {userId} or ADMIN | List API keys for a user |
| `DELETE` | `/api/v1/users/{userId}/apikeys/{apiKeyId}` | Owner of {userId} or ADMIN | Revoke an API key |
| `GET` | `/api/v1/data` | USER or ADMIN | Example protected read endpoint |
| `POST` | `/api/v1/data` | ADMIN only | Example protected write endpoint |

API key endpoints use ownership-based authorization: regular users can only create, list, and revoke keys for their
own userId, while admins can manage any user’s keys.

## Request Flow

1. Register a user through `/api/v1/users`
2. Log in through `/api/v1/login`
3. Use the JWT for authenticated requests
4. Generate an API key for the target user
5. Send the API key in `x-api-key` when calling protected data endpoints

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
- Expired or revoked API keys are rejected by the validation layer
- Custom exception handlers return JSON error envelopes instead of default HTML responses
