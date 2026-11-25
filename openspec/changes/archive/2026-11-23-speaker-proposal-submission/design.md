# Design

## Context

The `proposal` package is an empty BCE scaffold. Existing conventions this
design must follow: controls inject only repositories (never other controls),
exceptions carry their own HTTP status and are reused across features, Flyway
owns the schema (`validate` mode), identity reaches control as the caller's
ena string via `jwt.getName()` (JWT `upn` = email), and `@RolesAllowed` sits
on the resource. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**

- Speaker submits a standalone proposal (title + description) to an OPEN CFP.
- Speaker lists and reads back only their own proposals.
- Reuse existing exceptions/mappers so error shape stays consistent.

**Non-Goals:**

- Proposal status, committee review, accept/reject (future change adds a
  `status` column via a new migration).
- Speaker withdraw/delete of submissions.
- Any draft↔proposal relationship (explicitly discarded — see Decisions).
- Submission deadlines; `CfpStatus.OPEN` is the only gate.

## Decisions

### Standalone proposal, no draft linkage

`ProposalRequest` is `{ title, description }` with plain bean validation —
identical in shape to `DraftRequest`. Alternatives considered during
exploration: snapshot-with-optional-`draftId` and live FK to the draft; both
were rejected so drafts remain a private scratchpad with no coupling to the
submission flow. Consequence accepted: drafts currently have no downstream
consumer.

### Endpoint layout

- `POST /api/events/{eventId}/cfp/proposals` — submission, nested under the
  existing CFP path so the target CFP is unambiguous in the URL.
- `GET /api/proposals`, `GET /api/proposals/{id}` — flat collection for the
  caller's own submissions.

A separate `ProposalResource` owns these paths (feature-first: `proposal`
owns its boundary even when nested under `events/{id}/cfp`). Alternative —
`POST /api/proposals` with `eventId` in the body — was rejected as less
consistent with the existing nested CFP resource.

### Control: `ProposalSubmission`

Named after its responsibility per AGENTS.md (siblings use `*Management`, but
the rule prefers use-case names like `EventCreation`/`UserRegistration`). It
injects `EventRepository`, `CfpRepository`, `UserRepository`,
`ProposalRepository` and replicates the small event→CFP lookup itself, matching
the convention that controls orchestrate repositories and never other
controls. `CfpManagement.get()` was the alternative; rejected to keep
control→control coupling out of the codebase.

### Exception strategy

- Reused: `EventNotFoundException`/`CfpNotFoundException` (404),
  `InvalidCfpStateException` (409, "CFP is not open"), `InvalidCredentialsException`
  (valid token but user row gone — same as `DraftManagement`).
- New: `ProposalException` base + `ProposalExceptionMapper` (copy of the
  existing mapper pattern), `ProposalNotFoundException` (404),
  `ProposalAccessDeniedException` (403, non-owner read).
- Bean validation on `ProposalRequest` → 400 `{error, field}` via the shared
  `ConstraintViolationMapper`; no custom validation needed since both fields
  are unconditionally required.

### Schema (V8__create_proposals.sql)

`proposals(id, cfp_id → cfps, speaker_id → users, title, description,
submitted_at)`. No `status` or review columns (triage adds them later), no
secondary indexes (consistent with existing migrations), no draft FK.

### Response shape

`ProposalResponse(id, title, description, cfpId, eventId, eventName,
speakerId, speakerName, submittedAt)` — mirrors `DraftResponse`'s
owner fields and includes event context so "my proposals" is readable without
extra calls.

## Risks / Trade-offs

- [Duplicate submissions] → allowed by design (multiple talks per CFP is
  legitimate); dedup can be revisited when triage exists.
- [No withdraw endpoint] → a speaker cannot retract a submission; acceptable
  while CFPs are manually opened/closed, flagged for the triage change.
- [Lookup duplication] → the event→CFP resolution is copied from
  `CfpManagement`; ~5 lines, cheaper than introducing control→control calls.
- [TestDataCleaner ordering] → `delete from Proposal` must precede `Cfp` and
  `User` deletes or FK constraints fail; covered in tasks.

## Migration Plan

Standard Flyway flow: `V8` runs automatically at startup in all profiles;
Hibernate `validate` confirms entity mapping matches. Rollback is a schema
revert — no data migration concerns at this stage.
