# AGENTS.md

## Project

CFP Manager — Quarkus REST API (Java 25) for the Call for Papers lifecycle of tech events. Speakers register and submit talk proposals; coordinators register events, open CFPs and triage proposals.

## Stack

Quarkus 3.40 · quarkus-rest (JAX-RS) · Hibernate ORM with Panache · PostgreSQL · Flyway · JUnit 5 + REST Assured.

## Commands

```shell
./mvnw quarkus:dev                  # dev mode (live coding, Dev UI at /q/dev/)
./mvnw test                         # unit tests
./mvnw verify -DskipITs=false       # unit + integration tests (ITs are skipped by default)
./mvnw package                      # build → target/quarkus-app/quarkus-run.jar
./mvnw package -Dnative             # native executable
```

Dev/test use Quarkus Dev Services: a PostgreSQL container starts automatically (requires Docker/Podman). Prod config: `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` env vars.

## Architecture

Package-by-feature under `com.dearrudam` — each capability keeps its entity, REST resource, services and repositories together:

- `user` —  speaker/coordinator accounts
- `event` — event registration and management
- `cfp` — call for papers lifecycle
- `proposal` — talk proposal submission and triage
- `shared` — cross-cutting concerns only

Tests mirror the feature packages under `src/test/java`.

## Conventions

- Java 25 idioms; see `.agents/skills/java-conventions` (records, `var`, package-private over `private`, AssertJ for tests).
- Panache entities extend `PanacheEntity` with public fields; `@Transactional` on write endpoints, not on entities.
- REST resources exchange **DTOs** (records) for input and output — never expose Panache entities in the API; map entity ↔ DTO at the resource boundary.
- Input DTOs use bean validation (`@NotBlank`, `@Email`, …) — requires the `quarkus-hibernate-validator` extension.
- Error responses share a consistent JSON shape: `{ "error": "...", "field": "..." }` — `field` is included for input validation errors.
- REST resources are mounted under `/api` (e.g. `@Path("/api/users")`).
- **Schema is owned by Flyway**: add `V<n>__<description>.sql` in `src/main/resources/db/migration`. Hibernate runs with `schema-management.strategy=validate` — entity mappings must match the migrated schema or startup fails.
- Before writing Quarkus code, load extension patterns via the `quarkus-agent` MCP `quarkus_skills` tool.
- Code, comments and docs in English.
