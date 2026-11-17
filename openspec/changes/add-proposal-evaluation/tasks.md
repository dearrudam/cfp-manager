# Tasks

## 1. Persistence layer

- [x] 1.1 Create `src/main/resources/db/migration/V11__create_evaluations.sql` with the `evaluations` table (`proposal_id` → `proposals` `ON DELETE CASCADE`, `evaluator_id` → `users`, `score INTEGER NOT NULL`, `created_at`, `UNIQUE (proposal_id, evaluator_id)`); verify the file is picked up by Flyway on startup
- [x] 1.2 Create `Evaluation` entity (public fields, `ManyToOne` to `Proposal` and `User`, `createdAt` via `@CreationTimestamp`) and `EvaluationRepository` (`PanacheRepository` with `find(Proposal, User)` and `listByProposal(Proposal)`), plus `package-info.java` for `evaluation.entity`; verify `./mvnw compile` succeeds and a `@QuarkusTest` boots without Hibernate `validate` errors

## 2. Control layer

- [x] 2.1 Create `EvaluationException` (abstract, carries HTTP status), `EvaluationAccessDeniedException` (403) and `EvaluationAlreadyExistsException` (409) in `evaluation.control`, plus `package-info.java`; verify `./mvnw compile` succeeds
- [x] 2.2 Implement `EvaluationManagement.submit(proposalId, score, callerEmail)` (`@Transactional`) enforcing in order: proposal `404` via `ProposalManagement.get`, caller resolution, committee check via `CfpCommitteeRepository` → `403`, owner/co-author check via `Proposal.owner` + `ProposalSpeakerRepository.find` → `403`, `status != EVAL` → `409 InvalidProposalStateException`, duplicate via `EvaluationRepository.find` → `409`; verify `./mvnw compile` succeeds
- [x] 2.3 Implement `EvaluationManagement.list(proposalId, callerEmail)` applying proposal lookup, caller resolution and committee check, returning evaluations ordered by id; verify `./mvnw compile` succeeds

## 3. Boundary layer and API tests

- [x] 3.1 Create `EvaluationRequest` (`@NotNull @Min(1) @Max(5) Integer score`), `EvaluationResponse` record with `from` mapper, `EvaluationResource` at `/api/proposals/{proposalId}/evaluations` (`POST` + `GET`, `@Authenticated`, `jwt.getName()` passed to control), and `EvaluationExceptionMapper`, plus `package-info.java` for `evaluation.boundary`; verify `./mvnw compile` succeeds
- [x] 3.2 Add `delete from Evaluation` before `delete from Proposal` in `TestDataCleaner`; verify existing tests still pass
- [x] 3.3 Create `EvaluationResourceTest` (`@QuarkusTest` + REST Assured, mirroring `ProposalResourceTest` setup: coordinator creates event/CFP, adds committee member, speaker submits proposal, CFP close moves it to `EVAL`) covering every spec scenario — successful `201`, proposal `404`, non-`EVAL` `409`, non-member `403`, unauthenticated `401`, owner `403`, co-author `403`, duplicate `409`, invalid scores `0`/`6`/missing `400` with `field: "score"`, list `200`/`403`/`401`, and `405`/`404` on update/delete attempts; verify `./mvnw test` passes

## 4. Integration verification

- [x] 4.1 Run `./mvnw verify` and confirm the full build is green; run `openspec validate add-proposal-evaluation --strict` and confirm no warnings
