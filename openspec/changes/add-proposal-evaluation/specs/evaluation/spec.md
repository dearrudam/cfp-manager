# Spec Delta

## Purpose

Lets members of a CFP's review committee score talk proposals with integer grades from 1 to 5, under rules that protect the integrity of the review process.

## ADDED Requirements

### Requirement: Committee members can evaluate proposals in evaluation status

The system SHALL allow an authenticated committee member of a CFP to record an evaluation for a proposal that belongs to that CFP and is in `EVAL` status. The evaluation SHALL contain an integer score between 1 and 5 and SHALL record which evaluator submitted it.

#### Scenario: Successful evaluation

- **GIVEN** a proposal in `EVAL` status and an authenticated user who is a committee member of the proposal's CFP
- **WHEN** the user submits an evaluation with a score of 4
- **THEN** the system records the evaluation and responds with `201 Created` including the evaluation's id, score and evaluator

#### Scenario: Proposal does not exist

- **GIVEN** an authenticated committee member
- **WHEN** the user submits an evaluation for a proposal id that does not exist
- **THEN** the system responds with `404` and an error body

#### Scenario: Proposal is not in evaluation status

- **GIVEN** a proposal in `SUBMITTED`, `APPROVED` or `REJECTED` status
- **WHEN** a committee member of its CFP submits an evaluation
- **THEN** the system responds with `409` and an error body, and no evaluation is recorded

### Requirement: Only committee members may evaluate proposals

The system SHALL reject evaluation submissions from users who are not committee members of the CFP that owns the proposal, regardless of their role.

#### Scenario: Authenticated non-member is rejected

- **GIVEN** a proposal in `EVAL` status and an authenticated user who is not a committee member of its CFP
- **WHEN** the user submits an evaluation
- **THEN** the system responds with `403` and an error body, and no evaluation is recorded

#### Scenario: Unauthenticated request is rejected

- **WHEN** a request submits an evaluation without authentication
- **THEN** the system responds with `401`

### Requirement: Authors and co-authors cannot evaluate their own proposal

The system SHALL reject an evaluation when the evaluator is the proposal's owner or one of its co-authors (speakers), even if the evaluator is a committee member of the CFP.

#### Scenario: Owner attempts to evaluate own proposal

- **GIVEN** a proposal in `EVAL` status whose owner is a committee member of its CFP
- **WHEN** the owner submits an evaluation for that proposal
- **THEN** the system responds with `403` and an error body, and no evaluation is recorded

#### Scenario: Co-author attempts to evaluate the proposal

- **GIVEN** a proposal in `EVAL` status with a co-author who is a committee member of its CFP
- **WHEN** the co-author submits an evaluation for that proposal
- **THEN** the system responds with `403` and an error body, and no evaluation is recorded

### Requirement: Each evaluator evaluates a proposal at most once

The system SHALL allow at most one evaluation per evaluator per proposal. A second submission by the same evaluator for the same proposal SHALL be rejected.

#### Scenario: Duplicate evaluation is rejected

- **GIVEN** an evaluator who already submitted an evaluation for a proposal
- **WHEN** the same evaluator submits another evaluation for that proposal
- **THEN** the system responds with `409` and an error body, and the original evaluation is unchanged

### Requirement: Evaluation score is an integer between 1 and 5

The system SHALL validate that the submitted score is an integer within the inclusive range 1 to 5 and reject invalid values before any business rule is applied.

#### Scenario: Score below range

- **WHEN** a committee member submits an evaluation with score 0
- **THEN** the system responds with `400` and an error body identifying the score field

#### Scenario: Score above range

- **WHEN** a committee member submits an evaluation with score 6
- **THEN** the system responds with `400` and an error body identifying the score field

#### Scenario: Missing score

- **WHEN** a committee member submits an evaluation without a score
- **THEN** the system responds with `400` and an error body identifying the score field

### Requirement: Evaluations cannot be modified or removed

The system SHALL NOT expose any operation that updates or deletes a recorded evaluation. An evaluation, once recorded, remains unchanged.

#### Scenario: No mutation endpoint exists

- **WHEN** a client attempts to update or delete a recorded evaluation through the API
- **THEN** the system offers no such operation and rejects the request

### Requirement: Committee members can list a proposal's evaluations

The system SHALL allow an authenticated committee member of a CFP to retrieve the evaluations recorded for a proposal of that CFP, including the evaluator's identity and score.

#### Scenario: Committee member lists evaluations

- **GIVEN** a proposal with two recorded evaluations
- **WHEN** a committee member of its CFP requests the proposal's evaluations
- **THEN** the system responds with `200` and the list of evaluations ordered consistently

#### Scenario: Non-member cannot list evaluations

- **GIVEN** an authenticated user who is not a committee member of the proposal's CFP
- **WHEN** the user requests the proposal's evaluations
- **THEN** the system responds with `403` and an error body

#### Scenario: Unauthenticated listing is rejected

- **WHEN** a request lists a proposal's evaluations without authentication
- **THEN** the system responds with `401`
