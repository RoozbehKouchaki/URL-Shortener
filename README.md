# URL Shortener

A small URL shortening service: a Spring Boot backend that creates and resolves
short links, and an Angular frontend for signing in and managing them. Each
authenticated user can create short links, list their own links, deactivate
them, and view click statistics.

## Tech stack

- **Backend:** Java 21, Spring Boot 3.5 (Web, Data JPA, Security, Validation)
- **Database:** H2, file-based (links and accounts survive a restart)
- **Frontend:** Angular 19
- **Auth:** HTTP Basic against seeded demo accounts

## Architecture

```
                    HTTP Basic
  ┌────────────┐   (/api/**)      ┌──────────────────────────┐
  │  Angular   │ ───────────────► │  Spring Boot backend     │
  │  SPA       │                  │                          │
  │ (port 4200)│ ◄─────────────── │  Controllers             │
  └────────────┘   JSON responses │    ├─ LinkController      │
        ▲                         │    └─ RedirectController  │
        │ 302 redirect            │  Service (LinkService)    │
   ┌────┴─────┐   GET /{code}     │  Repositories (JPA)       │
   │ Visitor  │ ────────────────► │            │              │
   │ browser  │                   └────────────┼─────────────┘
   └──────────┘                                ▼
                                        ┌──────────────┐
                                        │  H2 (file)   │
                                        └──────────────┘
```

- **Angular SPA** — login and dashboard. Sends credentials as HTTP Basic on
  every `/api` call (stateless; no server session).
- **`LinkController`** — the authenticated management API (create, list,
  deactivate, stats).
- **`RedirectController`** — the public `GET /{shortCode}` path; resolves an
  active link, records the click, and returns a 302 to the long URL.
- **`LinkService`** — all business logic: URL validation, unique short-code
  generation, ownership checks, and atomic click counting.
- **JPA repositories over H2** — file-based storage, so links and accounts
  survive a restart.

## Project structure

```
URL_Shortener/
├── backend/     Spring Boot API + redirect service
└── frontend/    Angular single-page app
```

## Prerequisites

- JDK 21
- Node.js 18+ and npm (for the frontend)
- No local Maven or Angular CLI install required — the backend uses the Maven
  wrapper (`./mvnw`) and the frontend uses the CLI from its dev dependencies.

## Running the backend

```bash
cd backend
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`. The H2 file database is created
under `backend/data/` on first run, and the demo users are seeded automatically
at startup.

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

## API reference

All `/api/**` endpoints require HTTP Basic authentication. The redirect endpoint
is public.

| Method | Path                              | Description                                  |
| ------ | --------------------------------- | -------------------------------------------- |
| `POST` | `/api/links`                      | Create a short link (`{ "longUrl": "..." }`) |
| `GET`  | `/api/links`                      | List the caller's links, newest first        |
| `POST` | `/api/links/{shortCode}/deactivate` | Deactivate a link the caller owns           |
| `GET`  | `/api/links/{shortCode}/stats`    | Get the click count for a link the caller owns |
| `GET`  | `/{shortCode}`                    | Public redirect to the long URL (302)        |

Short codes are 7-character base62 strings generated randomly. A redirect to an
unknown or deactivated code returns 404; acting on a link you do not own returns
403. Errors are returned in a consistent JSON shape (`timestamp`, `status`,
`message`, and an optional `field`).

## Configuration

Backend settings live in `backend/src/main/resources/application.properties`.
The base address used to build short URLs is configurable:

```properties
app.base-address=http://localhost:8080
```

## Testing

```bash
cd backend
./mvnw test
```

The backend test suite includes integration tests covering the active-link
redirect (302), unknown/inactive lookups (404), and cross-user access (403).

## Scope notes

This is a demo. Production concerns such as caching, token-based auth (JWT),
and rate limiting are intentionally out of scope.
