# Design

## Context

The `com.dearrudam.evaluation` feature package exists but is empty. Proposals already carry the lifecycle this feature consumes: `ProposalStatus.EVAL` is set on all `SUBMITTED` proposals when `CfpManagement.close` runs. Committee membership is modeled by `CfpCommitteeMember` (unique per `cfp` + `user`), authorship by `Proposal.owner` plus `ProposalSpeaker` co-authors, and callers are identified by the JWT `upn` claim (`jwt.getName()` = email). Flyway owns the schema (`schema-management.strategy=validate`), so a new table requires a new migration.

## Goals / Non-Goals

**Goals:**

- Persist one immutable evaluation per (proposal, evaluator) with a score in [1, 5].
- Enforce committee membership, authorship exclusion, `EVAL`-only state and the once-per-evaluator rule in the control layer.
- Expose `POST` and `GET /api/proposals/{proposalId}/evaluations` returning DTOs, never entities.

**Non-Goals:**

- Evaluation comments or free-text feedback.
- Aggregate scores, averages, or using evaluations to drive proposal status transitions.
- Bulk evaluation, evaluation reminders, or speaker-facing visibility of scores.
- Changes to the proposal lifecycle or committee management.

## Decisions

### Data model: dedicated `evaluations` table

`V11__create_evaluations.sql` creates `evaluations(id, proposal_id → proposals ON DELETE CASCADE, evaluator_id → users, score INTEGER NOT NULL, created_at)` with `UNIQUE (proposal_id, evaluator_id)`. The unique constraint backstops the once-per-evaluator rule at the database level; the control-layer pre-check exists to return a friendly `409` instead of a constraint violation. No `updated_at` column — evaluations are immutable by design. Alternative considered: storing scores as JSON on the proposal — rejected because it cannot enforce per-evaluator uniqueness or immutability relationally.

### API: evaluations as a sub-resource of proposals

`EvaluationResource` mounts at `/api/proposals/{proposalId}/evaluations`, mirroring the existing `POST /api/proposals/{id}/speakers` nesting. Both endpoints use `@Authenticated` (not `@RolesAllowed`): committee membership — not a user role — is the authorization criterion, and members are not role-restricted in the model. The caller email comes from `jwt.getName()` and is passed into control, per project convention.

- `POST` accepts `EvaluationRequest(@NotNull @Min(1) @Max(5) Integer score)`; bean validation rejects bad scores with `400` + `field: "score"` via the shared `ConstraintViolationMapper` before any business rule runs.
- `GET` returns `List<EvaluationResponse>` ordered by evaluation id (insertion order, deterministic).

### Control: `EvaluationManagement` in `evaluation.control`

`submit(proposalId, score, callerEmail)` (`@Transactional`) and `list(proposalId, callerEmail)` enforce, in order:

1. Proposal lookup via `ProposalManagement.get` → `404 ProposalNotFoundException` (reuses proposal.control, same pattern as `CfpManagement` → `ProposalManagement`).
2. Caller resolution via `UserRepository.findByEmail` → `InvalidCredentialsException`.
3. Committee check via `CfpCommitteeRepository.findByUserId(proposal.cfp, user.id)` → `403 EvaluationAccessDeniedException`.
4. Authorship check: `proposal.owner.id == user.id` or `ProposalSpeakerRepository.find(proposal, user)` present → `403 EvaluationAccessDeniedException`.
5. State check: `proposal.status != EVAL` → `409 InvalidProposalStateException` (reused from proposal.control; `ProposalExceptionMapper` already maps the hierarchy).
6. Duplicate check: `EvaluationRepository.find(proposal, user)` present → `409 EvaluationAlreadyExistsException`.
7. Persist `Evaluation` and return `201`.

`list` applies steps 1–3 only — committee members may read evaluations regardless of proposal status. Reusing `ProposalNotFoundException`/`InvalidProposalStateException` avoids duplicating proposal-state semantics; evaluation-specific failures get `EvaluationException` (abstract, carries HTTP status like `ProposalException`), `EvaluationAccessDeniedException` (403) and `EvaluationAlreadyExistsException` (409), mapped by a new `EvaluationExceptionMapper` mirroring the other feature mappers.

### Immutability

No PUT/PATCH/DELETE endpoints exist for evaluations; the entity exposes no mutation path and the table has no `updated_at`. The spec records this as an absence-of-operation requirement.

### Test support

`TestDataCleaner` gains `delete from Evaluation` before `Proposal` (FK order). REST Assured tests follow `ProposalResourceTest` conventions: register coordinator/speakers, build event → CFP → committee → proposals moved to `EVAL` (via CFP close), then exercise the endpoints.

## Risks / Trade-offs

- [Concurrent duplicate submissions could surface a DB constraint violation (500) instead of 409] → Mitigation: unique constraint guarantees correctness; the pre-check covers the realistic sequential case, consistent with how `cfp_committee` handles the same pattern.
- [Committee membership is checked per request; a member removed mid-review keeps past evaluations but loses access] → Accepted: evaluations are historical records; removal only affects future access.
- [Listing exposes evaluator identities to all committee members] → Intended: score transparency is the point of committee review.

## Migration Plan

Additive only: `V11__create_evaluations.sql` runs automatically at startup via Flyway. No backfill needed (empty table). Rollback is dropping the table; no existing data is touched.
