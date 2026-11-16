# VinciSign Environment Baseline

**Status:** Runtime baseline for VS-FND-006; upload flow wiring remains pending
**Confirmed:** 2026-11-14  
**Scope:** Simple PDF/PDF-A document flow only

This document tells agents which environment values already exist and which values will be needed when the first document-storage ticket is implemented. Do not commit real secrets.

## Current template setup

| Area | Current state | Source |
|---|---|---|
| Local PostgreSQL | `infra/docker-compose.yml` starts PostgreSQL 16 and maps host port `5433` to container port `5432`; `infra/postgres/init.sql` creates database `app` | `infra/docker-compose.yml:1-15`; `infra/postgres/init.sql:1` |
| Backend local run | `apps/backend/.env.example` defaults to `localhost:5432`, so a backend run outside Docker must use `localhost:5433` when using `make dev` | `apps/backend/.env.example:1-15`; `Makefile:3-5` |
| Backend container run | Root `docker-compose.yml` connects the backend to the `postgres` service on container port `5432` | `docker-compose.yml:17-28` |
| Database migrations | Flyway is enabled; only template `users` and `todos` tables exist today | `apps/backend/src/main/resources/application.yml:8-13`; `apps/backend/src/main/resources/db/migration/` |
| Web authentication | Auth.js uses `AUTH_SECRET`, `AUTH_URL`, and optional Google/GitHub credentials | `apps/frontend/.env.example:1-17` |
| API URL | Frontend uses `NEXT_PUBLIC_API_BASE` or `NEXT_PUBLIC_API_BASE_URL` | `apps/frontend/.env.example:16-17` |
| Mobile | Out of scope; no mobile environment work is required | `AGENTS.md:20` |

## Minimum variables for the first implementation

### Backend

```dotenv
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/app # local make dev; use postgres:5432 inside root Compose
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<local-or-deployment-secret>
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000
APP_RATE_LIMIT_AUTH_ENABLED=true
APP_RATE_LIMIT_AUTH_CAPACITY=60
APP_RATE_LIMIT_AUTH_REFILL_PERIOD=1m
```

The JWT issuer variable already exists in the template, but the authentication implementation must verify the actual production issuer/provider contract before relying on it:

```dotenv
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=<configured-issuer>
```

### Supabase Storage — backend-only configuration

The backend storage boundary uses these server-only configuration values:

```dotenv
SUPABASE_URL=<supabase-project-url>
SUPABASE_SERVICE_ROLE_KEY=<server-only-secret>
SUPABASE_STORAGE_BUCKET=vincisign-documents
SUPABASE_STORAGE_SIGNED_URL_TTL_SECONDS=300
```

The service-role key must never be exposed to the frontend, committed to the repository, placed in a public `NEXT_PUBLIC_*` variable, or returned in an API response. The first flow does not need a frontend Supabase key, Redis variables, ClamAV variables, worker variables, conversion-service variables, notification-provider variables, or webhook secrets.

The initial bucket must be private. The API may create short-lived signed download access when a
later authorized document endpoint needs it; a signed URL is not stored as the durable document
reference. Object keys are opaque and must not be derived directly from project IDs, document IDs,
organization IDs, emails, or original filenames.

## Setup order

1. Reconcile the local PostgreSQL port used by the chosen run mode (`5433` for host-run backend with `make dev`; `postgres:5432` for root Compose).
2. Add project/document/evidence Flyway migrations; do not modify the template todo feature to represent VinciSign data.
3. Add the Supabase Storage adapter and server-only variables.
4. Create/configure a private document bucket and opaque object-key policy.
5. Add the synchronous PDF/PDF-A upload flow and integration tests.

No Redis, ClamAV, conversion worker, notification service, webhook delivery system, or external synchronization is part of this setup order.
