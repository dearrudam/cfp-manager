# Proposal

## Why

Speakers can register, write private drafts, and coordinators can run the CFP
lifecycle — but there is no way for a speaker to actually submit a talk
proposal. The `proposal` feature package exists as an empty scaffold, so the
core CFP flow dead-ends: an OPEN CFP accepts nothing.

## What Changes

- Add a `proposals` table (Flyway `V8`) linking a speaker to a CFP with a
  snapshot of the submitted `title` and `description`.
- Speakers can submit a proposal to an OPEN CFP via
  `POST /api/events/{eventId}/cfp/proposals` with `{ title, description }`.
- Speakers can list their own submissions (`GET /api/proposals`) and fetch a
  single one (`GET /api/proposals/{id}`).
- Submission is rejected when the CFP does not exist (404), is not OPEN (409),
  or the caller lacks the SPEAKER role (401/403).
- Proposals carry no review status and no link to drafts — committee triage
  (review, accept/reject) is explicitly out of scope for this change.

## Capabilities

### New Capabilities

- `proposal-submission`: Speakers submit standalone talk proposals (title +
  description) to an OPEN CFP and list their own submissions. Covers
  submission validation, ownership rules, and speaker-facing read access.

### Modified Capabilities

<!-- None: openspec/specs is empty — this is the project's first capability spec. -->

## Impact

- **New code**: `proposal` feature package — `entity` (`Proposal`,
  `ProposalRepository`), `control` (`ProposalSubmission`, `ProposalException`
  hierarchy), `boundary` (`ProposalResource`, `ProposalRequest`,
  `ProposalResponse`, `ProposalExceptionMapper`).
- **Schema**: `V8__create_proposals.sql`; Hibernate `validate` must match.
- **Tests**: `ProposalResourceTest` mirroring `DraftResourceTest`;
  `TestDataCleaner` must delete `Proposal` before `Cfp`/`User` (FK order).
- **API**: new endpoints under `/api/events/{eventId}/cfp/proposals` and
  `/api/proposals`; no existing endpoint changes.
- **Out of scope**: proposal status/review, committee triage endpoints,
  speaker withdraw/delete of submissions, draft↔proposal linkage, deadlines.
