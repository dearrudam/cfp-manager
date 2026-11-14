/// # Proposal
/// > Talk proposals submitted by speakers to an open call for papers.
///
/// ## Boundary
/// - `create-proposal` — submit a proposal to an event's open CFP
/// - `list-my-proposals` — list the caller's proposals
/// - `get-proposal` — return a proposal by id
/// - `update-proposal` — replace a proposal's content
/// - `add-co-speaker` — add a co-speaker to a proposal
///
/// ## Requirements
///
/// ### R1 Create a proposal
/// - R1.1 — When a speaker submits valid proposal data for an event whose CFP is open, the capability shall store it as submitted and respond as created (201).
/// - R1.2 — If the submission requests a draft copy, then the capability shall also keep a draft with the same content for the speaker.
/// - R1.3 — If the event does not exist, then the capability shall respond that the event was not found (404); if the event has no CFP, then the capability shall respond that the CFP was not found (404); if the CFP is not open, then the capability shall reject the submission as a conflict (409).
/// - R1.4 — If the proposal input is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
/// - R1.5 — If the caller is not an authenticated speaker, then the capability shall reject the request.
///
/// ### R2 List my proposals
/// - R2.1 — The capability shall return only the caller's proposals (200).
/// - R2.2 — If the caller is not an authenticated speaker, then the capability shall reject the request.
///
/// ### R3 Get a proposal
/// - R3.1 — While the proposal exists, the capability shall return it including its co-speakers to any authenticated caller (200).
/// - R3.2 — If the caller is unauthenticated, then the capability shall reject the request (401).
/// - R3.3 — If the proposal does not exist, then the capability shall respond that the proposal was not found (404).
///
/// ### R4 Update a proposal
/// - R4.1 — While the caller owns the proposal and its CFP is open, the capability shall replace its title, abstract, level, and duration and keep its status (200).
/// - R4.2 — If the caller does not own the proposal, then the capability shall reject the request as forbidden (403).
/// - R4.3 — If the proposal's CFP is not open, then the capability shall reject the update as a conflict (409).
/// - R4.4 — If the proposal does not exist, then the capability shall respond that the proposal was not found (404).
/// - R4.5 — If the caller is not an authenticated speaker, then the capability shall reject the request.
/// - R4.6 — If the update input is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
///
/// ### R5 Add a co-speaker
/// - R5.1 — While the caller owns the proposal and its CFP is open, when an existing user is added, the capability shall record the co-speaker and respond as created (201).
/// - R5.2 — If the user is already a co-speaker, then the capability shall reject the request as a conflict (409).
/// - R5.3 — If the user is the proposal owner, then the capability shall reject the request as a conflict (409).
/// - R5.4 — If the user does not exist, then the capability shall respond that the user was not found (404).
/// - R5.5 — If the proposal does not exist, then the capability shall respond that the proposal was not found (404).
/// - R5.6 — If the caller does not own the proposal, then the capability shall reject the request as forbidden (403).
/// - R5.7 — If the proposal's CFP is not open, then the capability shall reject the request as a conflict (409).
/// - R5.8 — If the caller is not an authenticated speaker, then the capability shall reject the request.
///
/// ## Entities
/// - Proposal
/// - ProposalSpeaker
/// - ProposalStatus
/// - TalkLevel
///
/// ## Out of scope
/// - Reviewer decisions on proposals (accept/reject triage)
/// - CFP lifecycle (owned by the cfp capability)
/// - Draft editing (owned by the draft capability)
package com.dearrudam.proposal;
