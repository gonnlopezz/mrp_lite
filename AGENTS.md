# MRP Lite — Agent Guide

## What this is

Spring Boot 3 + Angular 19 monolith (two Docker containers + PostgreSQL).  
University lab project — manufacturing resource planning (MRP).

## Dev workflow

```bash
./lpl build       # docker compose build
./lpl up          # start all services
./lpl down        # stop all services
./lpl restart backend|frontend|database   # restart one service
./lpl log backend|frontend|database       # tail logs for one service
./lpl sh backend|frontend|database        # open shell in container
./lpl test        # run Cucumber integration tests (docker run --rm)
./lpl mvn <args>  # run Maven inside backend container
./lpl staging <name>  # pipe staging/<name>.sql | psql to database
```

Single service restart uses docker compose restart (not down+up).  
Run E2E tests: `./lpl test` (uses `docker compose run --rm testing`).  
Load seed data: `./lpl staging data`, `./lpl staging clean`, etc.

## Architecture

| Layer | Tech | Entrypoint |
|---|---|---|
| Backend | Spring Boot 3.0.2 / Java 17 / Maven | `BackendApplication.java` |
| Frontend | Angular 19.2 (standalone, no NgModules) | `main.ts` → standalone bootstrap |
| DB | PostgreSQL (container: `database`) | `application.properties` |
| Integration tests | Cucumber.js | `testing/package.json` → `cucumber-js` |

**Backend package** (`unpsjb.labprog.backend`):
- `presenter/` — REST controllers (`@RestController`), English path like `/customers`, `/workshops`, `/orders`, `/plannings`
- `business/` — Services + JPA Repositories per domain
- `model/` — JPA entities (Lombok `@Getter @Setter @NoArgsConstructor`)
- `dto/` — Request DTOs for planning endpoints
- API responses wrapped in `{ status, message, data }` via `Response` utility

**Frontend** (`frontend/cli/`):
- Standalone components with `imports` array, no NgModules
- `skipTests: true` in angular.json for all generators
- Proxy: `/rest` → `http://backend:8080` (rewrite strips `/rest`)
- Services hit `rest/...`, use `DataPackage` / `ResultsPage` interfaces
- CRUD pattern: list component with pagination, detail component for create/edit
- Toast notifications via ngx-toastr; charts via ECharts (ngx-echarts) and Google Charts

## API map (backend endpoints)

| Base path | Domain | Notes |
|---|---|---|
| `/customers` | Cliente | page, search, CRUD |
| `/workshops` | Taller | page, search, CRUD; `/id/{id}/plannings` |
| `/equipment-types` | TipoEquipo | CRUD |
| `/products` | Producto | CRUD with task master-detail |
| `/orders` | Pedido | page (with `?state` filter), search, CRUD; `/planned`, `/id/{id}/plannings`, `/cuit/{cuit}/deliveryDate/{date}` |
| `/plannings` | Planificación | CRUD, `/filtered?workshopId=&orderId=`, `/order` (plan one order), `/pending` (batch all pending) |

Pagination: backend uses Spring Data Page (0-indexed). Frontend sends `page-1`.  
Validation errors → HTTP 409 (BusinessException).

## Planning engine

- Two strategies: `FORWARD` (from start date → forward), `BACKWARD` (from due date ← backward)
- `Planificador` picks best workshop; `PlanificacionCoordinador` orchestrates
- Orders: PENDIENTE → PLANIFICADO / NO_PLANIFICABLE → FINALIZADO
- No-replan guarantee: already-planned/finished orders' tasks are never moved

## Testing

- Cucumber.js BDD features in `testing/features/`, step defs in `step_definitions/`
- Run via Docker (profile `test`): `docker compose run --rm testing` or `./lpl test`
- Features expect backend running with specific staging data loaded first
- HTTP response assertions match on status code + message substring
- Planning tests check exact task schedules per equipment

## Key conventions

- English API paths, Spanish entity/component names (mixing is intentional)
- Frontend standalone components (no NgModule imports)
- Lombok for model boilerplate; `@NoArgsConstructor` required by JPA
- `@Getter @Setter` on entities (not `@Data`)
- `BusinessException` → 409 Conflict; `EntityNotFoundException` → 404
- New component/service: add `rest/...` prefix to API URLs in services
- Docker service hostnames: `backend`, `database`, `frontend`

## Reminders

- `docker-compose.yml` is gitignored — don't create/modify it, use `lpl` wrapper
- `./lpl` only supports the subcommands in its case block; for anything else edit docker-compose.yml directly or run raw docker commands
- `.gitignore` also ignores `lpl` (!) — the script is meant to stay local/tracked separately
