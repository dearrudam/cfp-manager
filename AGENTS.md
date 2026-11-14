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

BCE (Boundary/Control/Entity), organized feature-first under `com.dearrudam`. Each business capability is a top-level package split into three layers:

- `user` — speaker/coordinator accounts
- `event` — event registration and management
- `cfp` — call for papers lifecycle
- `draft` — speaker-owned talk drafts
- `proposal` — talk proposal submission and triage

Each feature package contains:

- `boundary` — presentation: JAX-RS resources mounted under `/api`, request/response DTO records, entity↔DTO mappers, feature exception mappers.
- `control` — business logic: use-case classes named after their responsibility (never `*Control`, `*Service` or `*Manager` suffixes) that orchestrate entities and enforce domain rules.
- `entity` — domain and persistence: plain JPA entities and `PanacheRepository` beans.

Dependencies point inward: `boundary → control → entity`. The entity layer never depends on control or boundary, and the boundary never exposes entities — DTO mapping happens at the resource. Cross-cutting code shared by all features (exception mappers, error envelope) lives in `shared`.

```text
src/main/java/com/dearrudam/
  user/       boundary/ · control/ · entity/
  event/      boundary/ · control/ · entity/
  cfp/        boundary/ · control/ · entity/
  draft/      boundary/ · control/ · entity/
  proposal/   boundary/ · control/ · entity/
  shared/     cross-cutting concerns
```

Tests mirror the BCE package structure under `src/test/java`.

## SDD4J

Spec source:
- format: `package-info.java` (Markdown doc comments, JEP 467)
- source root: `src/main/java`
- requirements style: EARS
- trace ids: `R<n>.<m>`

Spec language:
- default: `en`
- requirements: `pt-BR`

Architecture layout:
- skill: `sdd4j-bce`
- scope: primary project architecture
- component package pattern: `com.dearrudam.<component>`
- capability spec: the component package's `package-info.java` (e.g. `com.dearrudam.user`)
- layer packages: `boundary` (entrypoints/DTOs), `control` (use cases, no spec section), `entity` (domain types)
- components: `user`, `event`, `cfp`, `draft`, `proposal`, `review`, `shared`
- `shared` is a flat package (no layer split): its `## Boundary` maps to the package's public entrypoints (exception mappers, constraint annotations) and `## Entities` to its public types
- test package mirrors main package: true

Stack:
- skill: project conventions (`AGENTS.md`, `.agents/skills/java-conventions`, `.agents/skills/java-distiller`)
- build tool: Maven (`./mvnw`)
- verification command: `./mvnw test` (unit); `./mvnw verify -DskipITs=false` for integration tests

Traceability:
- trace convention: per-component `{Component}Requirement` annotation (e.g. `UserRequirement`, `SharedRequirement`) generated in the component root package, with a nested `Rn` enum — one constant per statement, `toString()` returning the literal spec id
- requirement id must resolve to its exact runner-visible `Rn.m` form through a display name, case label, symbol, or annotation consumed by the test/reporting infrastructure
- a normalized Java identifier such as `R1_2` is valid only when the configured infrastructure resolves and displays it as `R1.2`
- JavaDoc and comments alone do not count

## Conventions

- Java 25 idioms; see `.agents/skills/java-conventions` (records, `var`, package-private over `private`, AssertJ for tests).
- Aim for clean, idiomatic, well-crafted code — load `.agents/skills/java-distiller` to simplify, modernize and beautify Java code.
- Entities are plain JPA classes with public fields; data access goes through `PanacheRepository` beans — repository pattern, no active record. `@Transactional` on control write methods, never on entities or repositories.
- REST resources exchange **DTOs** (records) for input and output — never expose Panache entities in the API; map entity ↔ DTO at the resource boundary.
- Input DTOs use bean validation (`@NotBlank`, `@Email`, …) — requires the `quarkus-hibernate-validator` extension.
- Error responses share a consistent JSON shape: `{ "error": "...", "field": "..." }` — `field` is included for input validation errors.
- REST resources are mounted under `/api` (e.g. `@Path("/api/users")`).
- Protected endpoints use `@Authenticated` (or `@RolesAllowed`); the caller's identity comes from the JWT `upn` claim via `JsonWebToken` — pass it to control methods, never let control depend on `JsonWebToken`.
- **Schema is owned by Flyway**: add `V<n>__<description>.sql` in `src/main/resources/db/migration`. Hibernate runs with `schema-management.strategy=validate` — entity mappings must match the migrated schema or startup fails.
- Before writing Quarkus code, load extension patterns via the `quarkus-agent` MCP `quarkus_skills` tool.
- Code, comments and docs in English.
