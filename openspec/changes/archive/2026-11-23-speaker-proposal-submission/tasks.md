# Tasks

## 1. Persistence

- [x] 1.1 Add `src/main/resources/db/migration/V8__create_proposals.sql` creating `proposals(id, cfp_id → cfps, speaker_id → users, title, description, submitted_at)` — verify Flyway applies V8 on startup (`./mvnw quarkus:dev` boots with no migration errors)
- [x] 1.2 Add `Proposal` entity and `ProposalRepository` in `proposal/entity` — `PanacheRepository` with a `listBySpeakerEmail` finder ordered by `submittedAt` desc — verify Hibernate `schema-management.strategy=validate` passes at startup

## 2. Control

- [x] 2.1 Add `ProposalException` base plus `ProposalNotFoundException` (404) and `ProposalAccessDeniedException` (403) in `proposal/control`, mirroring the `DraftException` pattern
- [x] 2.2 Implement `ProposalSubmission` in `proposal/control` injecting `EventRepository`, `CfpRepository`, `UserRepository`, `ProposalRepository`: `submit(eventId, title, description, callerEmail)` resolves event→CFP, requires `CfpStatus.OPEN` (reuse `InvalidCfpStateException`), resolves the speaker by email (reuse `InvalidCredentialsException`), persists the proposal; `list(callerEmail)` and `get(id, callerEmail)` enforce ownership (reuse `EventNotFoundException`/`CfpNotFoundException`) — verify behavior through the `ProposalResourceTest` scenarios in group 4

## 3. Boundary

- [x] 3.1 Add `ProposalRequest` record with `@NotBlank` `title` and `description`, and `ProposalResponse` record + `from(Proposal)` mapping (id, title, description, cfpId, eventId, eventName, speakerId, speakerName, submittedAt)
- [x] 3.2 Add `ProposalResource` in `proposal/boundary`: `@Path("/api/events/{eventId}/cfp/proposals")` POST for submission and `@Path("/api/proposals")` GET list + `@Path("/api/proposals/{id}")` GET single, class-level `@RolesAllowed("SPEAKER")`, caller email from `jwt.getName()` — verify endpoints return 401 unauthenticated and 403 for coordinator-only callers
- [x] 3.3 Add `ProposalExceptionMapper` `@Provider` mapping `ProposalException` to its status with the shared `ErrorResponse` shape

## 4. Tests

- [x] 4.1 Update `TestDataCleaner` to `delete from Proposal` before `Cfp` and `User` deletes — verify existing tests still pass (`./mvnw test`)
- [x] 4.2 Add `ProposalResourceTest` mirroring `DraftResourceTest` covering every spec scenario: successful submission (201), multiple submissions to same CFP, unknown event/CFP (404), CLOSED CFP (409), unauthenticated (401), coordinator-only (403), blank/missing title and description (400 with `field`), own-only list newest-first, get own (200), other's proposal (403), missing proposal (404) — verify `./mvnw test -Dtest=ProposalResourceTest` is green

## 5. Integration check

- [x] 5.1 Run `./mvnw verify` and confirm the full suite passes (ITs remain skipped by default)
- [x] 5.2 Run `./mvnw package` and confirm `target/quarkus-app/quarkus-run.jar` is produced

## Workflow follow-up

- Archive the change after implementation is verified.
