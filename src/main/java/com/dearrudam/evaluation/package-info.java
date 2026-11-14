/// # Proposal Evaluation
/// > Scores recorded by a CFP's review committee on proposals under evaluation.
///
/// ## Boundary
/// - `submit-evaluation` — record a committee member's score for a proposal
///
/// ## Requirements
///
/// ### R1 Submit an evaluation
/// - R1.1 — While the caller is an authenticated committee member of the proposal's CFP, the proposal is under evaluation, the caller is neither its author nor a co-author, and the caller has not evaluated it yet, when a score between 1 and 5 is submitted, the capability shall record the evaluation and respond as created (201).
/// - R1.2 — If the caller is unauthenticated, then the capability shall reject the request (401).
/// - R1.3 — If the caller is not a committee member of the proposal's CFP, then the capability shall reject the request as forbidden (403).
/// - R1.4 — If the caller is the proposal's author or one of its co-authors, then the capability shall reject the request as forbidden (403).
/// - R1.5 — If the proposal is not under evaluation, then the capability shall reject the request as a conflict (409).
/// - R1.6 — If the caller has already evaluated the proposal, then the capability shall reject the request as a conflict (409).
/// - R1.7 — If the proposal does not exist, then the capability shall respond that the proposal was not found (404).
/// - R1.8 — If the score is missing or outside the range 1 to 5, then the capability shall reject it as a validation error identifying the offending field (400).
/// - R1.9 — The capability shall not alter or remove a recorded evaluation.
///
/// ## Entities
/// - Evaluation
///
/// ## Out of scope
/// - Reviewer decisions on proposals (accept/reject triage)
/// - Aggregating scores or ranking proposals
/// - Proposal lifecycle and submission rules (owned by the proposal capability)
/// - CFP lifecycle and committee membership (owned by the cfp capability)
package com.dearrudam.evaluation;
