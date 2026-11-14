/// # Call for Papers
/// > The call for papers of an event: its lifecycle and review committee.
///
/// ## Boundary
/// - `get-cfp` — return the CFP of an event
/// - `create-cfp` — create the event's CFP in closed state
/// - `open-cfp` — open the CFP for submissions
/// - `close-cfp` — close the CFP
/// - `list-committee` — list the CFP's committee members
/// - `add-committee-member` — add a user to the committee
/// - `remove-committee-member` — remove a user from the committee
///
/// ## Requirements
///
/// ### R1 Get a CFP
/// - R1.1 — When the event exists and has a CFP, the capability shall return its status and lifecycle timestamps (200).
/// - R1.2 — If the event does not exist, then the capability shall respond that the event was not found (404); if the event has no CFP, then the capability shall respond that the CFP was not found (404).
/// - R1.3 — The capability shall serve CFP reads without requiring authentication.
///
/// ### R2 Create a CFP
/// - R2.1 — When the event owner creates a CFP for an event that has none, the capability shall create it in closed state and respond as created (201).
/// - R2.2 — If the event already has a CFP, then the capability shall reject the creation as a conflict (409).
/// - R2.3 — If the event does not exist, then the capability shall respond that the event was not found (404).
/// - R2.4 — If the caller is not the event owner, then the capability shall reject the request as forbidden (403).
/// - R2.5 — If the caller is unauthenticated, then the capability shall reject the request (401).
///
/// ### R3 Open a CFP
/// - R3.1 — While the CFP is closed and has at least one committee member, when the owner opens it, the capability shall mark it open, record the opening instant, and clear the closing instant (200).
/// - R3.2 — If the CFP is already open, then the capability shall reject the request as a conflict (409).
/// - R3.3 — If the CFP has no committee members, then the capability shall reject the opening as a conflict (409).
/// - R3.4 — If the event does not exist or has no CFP, then the capability shall respond not found (404).
/// - R3.5 — If the caller is not the event owner, then the capability shall reject the request as forbidden (403).
/// - R3.6 — If the caller is unauthenticated, then the capability shall reject the request (401).
///
/// ### R4 Close a CFP
/// - R4.1 — While the CFP is open, when the owner closes it, the capability shall mark it closed, record the closing instant, and move its submitted proposals to evaluation (200).
/// - R4.2 — If the CFP is already closed, then the capability shall reject the request as a conflict (409).
/// - R4.3 — If the event does not exist or has no CFP, then the capability shall respond not found (404).
/// - R4.4 — If the caller is not the event owner, then the capability shall reject the request as forbidden (403).
/// - R4.5 — If the caller is unauthenticated, then the capability shall reject the request (401).
///
/// ### R5 List the committee
/// - R5.1 — The capability shall return the CFP's committee members (200).
/// - R5.2 — The capability shall serve committee reads without requiring authentication.
/// - R5.3 — The capability shall order the listing by member name.
/// - R5.4 — If the event does not exist or has no CFP, then the capability shall respond not found (404).
///
/// ### R6 Add a committee member
/// - R6.1 — When the owner adds an existing user, the capability shall record the membership and respond as created (201).
/// - R6.2 — If the user is already a committee member, then the capability shall reject the request as a conflict (409).
/// - R6.3 — If the user does not exist, then the capability shall respond that the user was not found (404).
/// - R6.4 — If the event does not exist or has no CFP, then the capability shall respond not found (404).
/// - R6.5 — If the caller is not the event owner, then the capability shall reject the request as forbidden (403).
/// - R6.6 — If the caller is unauthenticated, then the capability shall reject the request (401).
///
/// ### R7 Remove a committee member
/// - R7.1 — When the owner removes a member and at least one other member remains, the capability shall delete the membership and respond with no content (204).
/// - R7.2 — If the member does not belong to the committee, then the capability shall respond that the member was not found (404).
/// - R7.3 — If the member is the last one, then the capability shall reject the removal as a conflict (409).
/// - R7.4 — If the event does not exist or has no CFP, then the capability shall respond not found (404).
/// - R7.5 — If the caller is not the event owner, then the capability shall reject the request as forbidden (403).
/// - R7.6 — If the caller is unauthenticated, then the capability shall reject the request (401).
///
/// ## Entities
/// - Cfp
/// - CfpCommitteeMember
/// - CfpStatus
///
/// ## Out of scope
/// - Proposal submission rules (owned by the proposal capability)
/// - Event registration (owned by the event capability)
/// - Reviewer decisions on proposals
package com.dearrudam.cfp;
