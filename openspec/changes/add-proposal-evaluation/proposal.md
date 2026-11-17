# Proposal

## Why

When a CFP closes, its proposals move to `EVAL` status and the review committee needs a way to score them. Today there is no mechanism to record evaluations, so coordinators cannot collect structured scores from committee members before deciding which proposals to approve or reject.

## What Changes

- Add a persisted `Evaluation` record linking a proposal, an evaluator (user) and an integer score from 1 to 5.
- Committee members of a CFP can submit an evaluation for a proposal of that CFP via `POST /api/proposals/{proposalId}/evaluations`.
- Committee members can list the evaluations of a proposal via `GET /api/proposals/{proposalId}/evaluations`.
- Enforce the business rules: only committee members may evaluate; authors and co-authors cannot evaluate their own proposal; each evaluator evaluates a proposal at most once; only proposals in `EVAL` status can be evaluated; evaluations are immutable once recorded.
- Add a Flyway migration for the new `evaluations` table and the `evaluation` feature package (entity, repository, control use case, REST resource, DTOs, exception mapper).

## Capabilities

### New Capabilities

- `evaluation`: Recording and reading of committee scores (1–5) for talk proposals, including the authorization and state rules that govern who may evaluate and when.

### Modified Capabilities

<!-- None: no existing spec-level behavior changes. The proposal lifecycle (SUBMITTED → EVAL → APPROVED/REJECTED) is unchanged; evaluations are a new capability that consumes the existing EVAL state. -->

## Impact

- **Code**: new files under `com.dearrudam.evaluation` (`entity`, `control`, `boundary`); `TestDataCleaner` learns about the new entity.
- **API**: new endpoints `POST`/`GET /api/proposals/{proposalId}/evaluations`; error responses keep the shared `{ "error": "..." }` envelope.
- **Database**: new `V11__create_evaluations.sql` migration; Hibernate `validate` requires the entity mapping to match.
- **Dependencies**: no new libraries; reuses Panache, bean validation and the existing JWT authentication.
