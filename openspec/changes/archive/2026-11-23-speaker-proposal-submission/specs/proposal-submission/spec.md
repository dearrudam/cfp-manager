# Spec Delta

## Purpose

Lets speakers submit standalone talk proposals (title and description) to an
OPEN CFP and review their own submissions, without any committee review step.

## ADDED Requirements

### Requirement: Speaker submits a proposal to an open CFP
The system SHALL allow an authenticated user with the SPEAKER role to submit a
proposal containing a title and description to a CFP whose status is OPEN. The
proposal content is provided directly in the request; no draft or other
external reference is accepted. Each proposal SHALL be recorded against the
caller's account and the CFP, with a submission timestamp.

#### Scenario: Successful submission
- **WHEN** a speaker submits a valid title and description to an OPEN CFP
- **THEN** the system returns 201 with the created proposal, recorded under the caller's account and the CFP

#### Scenario: Multiple submissions to the same CFP
- **WHEN** a speaker who already has a proposal submits another valid proposal to the same OPEN CFP
- **THEN** the system accepts it and records a second, independent proposal

### Requirement: Submission requires an existing open CFP
The system SHALL reject proposal submission when the target event or CFP does
not exist, or when the CFP status is not OPEN.

#### Scenario: Event or CFP not found
- **WHEN** a speaker submits a proposal to an unknown event or to an event without a CFP
- **THEN** the system returns 404 and records nothing

#### Scenario: CFP is closed
- **WHEN** a speaker submits a proposal to a CFP whose status is CLOSED
- **THEN** the system returns 409 and records nothing

### Requirement: Proposal endpoints require the speaker role
The system SHALL restrict proposal submission and read access to authenticated
users holding the SPEAKER role.

#### Scenario: Unauthenticated caller
- **WHEN** a request to a proposal endpoint carries no valid credentials
- **THEN** the system returns 401

#### Scenario: Caller without the speaker role
- **WHEN** an authenticated user holding only the COORDINATOR role submits or lists proposals
- **THEN** the system returns 403

### Requirement: Proposal content is validated
The system SHALL require a non-blank title and a non-blank description on
submission, rejecting invalid input before anything is recorded.

#### Scenario: Missing or blank title
- **WHEN** a speaker submits a proposal without a title or with a blank title
- **THEN** the system returns 400 identifying the title field

#### Scenario: Missing or blank description
- **WHEN** a speaker submits a proposal without a description or with a blank description
- **THEN** the system returns 400 identifying the description field

### Requirement: Speaker lists own proposals
The system SHALL let a speaker list their own proposals across all CFPs, most
recently submitted first.

#### Scenario: List contains only the caller's proposals
- **WHEN** a speaker requests their proposal list
- **THEN** the system returns only proposals submitted by that speaker, newest first

#### Scenario: Speaker with no submissions
- **WHEN** a speaker who never submitted requests their proposal list
- **THEN** the system returns an empty list

### Requirement: Speaker views a single own proposal
The system SHALL let a speaker retrieve one of their own proposals by id,
without exposing proposals submitted by others.

#### Scenario: Owner retrieves proposal
- **WHEN** a speaker requests a proposal they submitted
- **THEN** the system returns 200 with the proposal details

#### Scenario: Proposal belongs to another speaker
- **WHEN** a speaker requests a proposal submitted by a different speaker
- **THEN** the system returns 403 and reveals nothing about the proposal

#### Scenario: Proposal does not exist
- **WHEN** a speaker requests a proposal id that does not exist
- **THEN** the system returns 404
