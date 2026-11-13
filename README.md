# CFP Manager

CFP Manager is a REST API built with [Quarkus](https://quarkus.io/) for managing the Call for Papers (CFP) lifecycle of tech events.

It connects two kinds of users:

- **Speakers** — register an account and submit talk proposals to open Calls for Papers.
- **Coordinators** — register events, open and manage Calls for Papers, and receive and triage talk proposals.

## Domain

The application covers the end-to-end flow of a Call for Papers:

- **Users** — speaker and coordinator accounts.
- **Events** — conference/meetup registration (name, dates, venue, organizers), managed by coordinators.
- **Calls for Papers** — opening and closing submission windows per event, with tracks, formats and review criteria, managed by coordinators.
- **Proposals** — talk submissions received during an open CFP (title, abstract, speakers, level, duration), submitted by speakers and triaged by coordinators.

## Tech stack

- **Java 25** on **Quarkus 3.40**
- **quarkus-rest** for HTTP endpoints (JAX-RS)
- **Hibernate ORM with Panache** (`quarkus-hibernate-orm-panache`) for persistence
- **PostgreSQL** (`quarkus-jdbc-postgresql`)
- **Flyway** (`quarkus-flyway`) for schema migrations
- **JUnit 5 + REST Assured** for tests

## Prerequisites

- JDK 25
- Docker or Podman (only for local development — Quarkus Dev Services auto-starts a PostgreSQL container)

## Running the application in dev mode

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_** Quarkus ships with a Dev UI, available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it's not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/c4p-manager-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Data access

The application uses **Hibernate ORM with Panache** (`quarkus-hibernate-orm-panache`) on top of a **PostgreSQL** datasource (`quarkus-jdbc-postgresql`).

- In dev and test mode, [Dev Services](https://quarkus.io/guides/datasource#dev-services) automatically starts a PostgreSQL container — no configuration needed (requires Docker/Podman).
- In production, set `DATABASE_URL`, `DATABASE_USER` and `DATABASE_PASSWORD` (see `src/main/resources/application.properties`).
- Schema migrations are managed by [Flyway](https://quarkus.io/guides/flyway) (`quarkus-flyway`): SQL files in `src/main/resources/db/migration` (`V<n>__<description>.sql`) run automatically on startup in every profile. Hibernate schema generation is disabled; `schema-management.strategy=validate` makes Hibernate verify entity mappings against the migrated schema at startup.

Entities should extend `PanacheEntity` (active record) or use a `PanacheRepository`. See the [Hibernate ORM with Panache guide](https://quarkus.io/guides/hibernate-orm-panache).

## Testing

```shell script
./mvnw test                              # unit tests
./mvnw verify -DskipITs=false            # unit + integration tests
```

## Architecture

The codebase follows a **package-by-feature** organization: each business capability lives in its own top-level package under `com.dearrudam`, keeping all related classes — domain entities, REST resources, services and repositories — together. Code shared across features goes in a `shared` package.

## Project structure

```text
src/main/java/com/dearrudam/
  user/        # user accounts (speakers and coordinators)
  event/       # event registration and management
  cfp/         # call for papers lifecycle
  proposal/    # talk proposal submission and triage
  shared/      # cross-cutting concerns
src/main/resources/
  application.properties             # configuration (env-var driven)
  db/migration/                      # Flyway migrations (V<n>__<description>.sql)
src/test/java/com/dearrudam/        # JUnit tests (mirrors feature packages)
src/main/docker/                    # container Dockerfiles
```
