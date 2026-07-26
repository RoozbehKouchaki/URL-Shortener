# URL Shortener

A small URL shortening service: a Spring Boot backend that creates and resolves
short links, and an Angular frontend for signing in and managing them. Each
authenticated user can create short links, list their own links, deactivate
them, and view click statistics.

## Tech stack

- **Backend:** Java 21, Spring Boot 3.5 (Web, Data JPA, Security, Validation)
- **Database:** PostgreSQL 17, started from `compose.yml` by Spring Boot
- **Frontend:** Angular 19
- **Auth:** JWT bearer tokens against seeded demo accounts

## Architecture

```
                  Bearer <JWT>
  ┌────────────┐   (/api/**)      ┌──────────────────────────┐
  │  Angular   │ ───────────────► │  Spring Boot backend     │
  │  SPA       │                  │                          │
  │ (port 4200)│ ◄─────────────── │  Controllers             │
  └────────────┘   JSON responses │    ├─ AuthController      │
        ▲                         │    ├─ LinkController      │
        │ 302 redirect            │    └─ RedirectController  │
   ┌────┴─────┐   GET /{code}     │  Services, Repositories   │
   │ Visitor  │ ────────────────► │            │              │
   │ browser  │                   └────────────┼─────────────┘
   └──────────┘                                ▼
                                        ┌──────────────┐
                                        │  PostgreSQL  │
                                        └──────────────┘
```

- **Angular SPA** — login and dashboard. Signs in once, then sends a bearer
  token on every `/api` call.
- **`AuthController`** — sign in. The only place a password is checked.
- **`LinkController`** — the authenticated management API (create, list,
  deactivate, stats).
- **`RedirectController`** — the public `GET /{shortCode}` path; resolves an
  active link, records the click, and returns a 302 to the long URL.
- **`LinkService`** — all business logic: URL validation, unique short-code
  generation, ownership checks, and atomic click counting.
- **JPA repositories over PostgreSQL** — a named Docker volume keeps links and
  accounts across restarts.

## Project structure

```
URL_Shortener/
├── backend/     Spring Boot API + redirect service
└── frontend/    Angular single-page app
```

## Prerequisites

- JDK 21
- Docker with Docker Compose 2.2.0+ (for the database)
- Node.js 18+ and npm (for the frontend)
- No local Maven, Angular CLI, or PostgreSQL install required — the backend uses
  the Maven wrapper (`./mvnw`), the frontend uses the CLI from its dev
  dependencies, and the database runs in a container.

## Running the backend

```bash
cd backend
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`, and the demo users are seeded at
startup.

No database setup is needed. The `spring-boot-docker-compose` module starts the
PostgreSQL service defined in `backend/compose.yml`, supplies the connection
details to the application, and stops the container on shutdown. Because the
data lives in a named volume, links and accounts survive a restart.

This is why `application.properties` contains no `spring.datasource.*` entries:
locally they come from the running container, and deployed environments provide
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and
`SPRING_DATASOURCE_PASSWORD` instead. The same build artifact runs in both.

To run the database by hand instead, `docker compose up -d` in `backend/`.

## Running the frontend

```bash
cd frontend
npm install
npm start
```

The app runs on `http://localhost:4200`. API calls to `/api` are proxied to the
backend on port 8080 via `proxy.conf.json`, so run the backend alongside it.

## Demo accounts

Two accounts are seeded on startup for signing in and testing ownership rules:

| Username | Password         |
| -------- | ---------------- |
| `alice`  | `alice-password` |
| `bob`    | `bob-password`   |

## Authentication

Sign-in exchanges the password for a JWT access token (15 minutes), returned in
the response body. The client keeps it in memory and sends it as
`Authorization: Bearer <token>`. It is verified by signature alone, so no
database or session lookup happens while serving a request, and any instance can
serve any request.

Chosen over server-side sessions because sessions need a shared store,
replicated across data centers and consulted on every authenticated request.
The trade-off is that a signed token cannot be withdrawn before it expires,
which is why the lifetime is minutes.

```bash
# Sign in
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"alice-password"}'

# Call the API with the access token
curl -H "Authorization: Bearer <accessToken>" http://localhost:8080/api/links
```

## API reference

All `/api/**` endpoints require a bearer token, except `/api/auth/login` and
`/api/auth/signup`. The redirect endpoint is public.

| Method | Path                              | Description                                  |
| ------ | --------------------------------- | -------------------------------------------- |
| `POST` | `/api/auth/login`                 | Exchange credentials for an access token     |
| `POST` | `/api/auth/signup`                | Register an account and receive a token      |
| `POST` | `/api/links`                      | Create a short link (`{ "longUrl": "..." }`) |
| `GET`  | `/api/links`                      | List the caller's links, newest first        |
| `GET`  | `/api/links/{shortCode}`          | Get one link the caller owns, including its click count |
| `POST` | `/api/links/{shortCode}/deactivate` | Deactivate a link the caller owns           |
| `GET`  | `/{shortCode}`                    | Public redirect to the long URL (302)        |

Short codes are 7-character base62 strings generated randomly. A redirect to an
unknown or deactivated code returns 404; acting on a link you do not own returns
403. Errors are returned in a consistent JSON shape (`timestamp`, `status`,
`message`, and an optional `field`).

## Configuration

Backend settings live in `backend/src/main/resources/application.properties`.

```properties
# Base address used to build short URLs
app.base-address=http://localhost:8080

# JWT signing. The default is a development-only fallback; supply
# APP_JWT_SECRET everywhere else. HS256 requires at least 32 bytes.
app.jwt.secret=${APP_JWT_SECRET:...}
app.jwt.issuer=urlshortener
app.jwt.access-token-ttl=15m

# Demo-only. Production would use Flyway migrations with ddl-auto=validate.
spring.jpa.hibernate.ddl-auto=update
```

Signing is symmetric (HS256) to keep the demo to a single configuration value.
For a multi-service or multi-region deployment the next step is RS256, with the
private key in a secret manager and the public key published via a JWKS
endpoint, so validating instances never need the signing key.

## Testing

```bash
cd backend
./mvnw test
```

The backend test suite includes integration tests covering the active-link
redirect (302), unknown/inactive lookups (404), cross-user access (403), and
rejection of missing and tampered tokens (401).

Tests run against in-memory H2 rather than PostgreSQL, so they need no Docker
and stay fast. The trade-off is that they do not catch dialect differences;
Testcontainers with a real PostgreSQL would be the production-grade choice.

## Scope notes

This is a demo. Production concerns such as caching and rate limiting are
intentionally out of scope. The deliberate omissions are:

- **Refresh tokens** — a page reload signs the user out, and sign-out cannot
  revoke an already-issued access token.
- **RS256 with a JWKS endpoint** — signing is symmetric, so every instance needs
  the shared secret.
- **Versioned migrations** — `ddl-auto=update` lets Hibernate manage the schema.
  Production needs Flyway with `ddl-auto=validate`.
- **Testcontainers** — tests use H2 rather than the real database.
