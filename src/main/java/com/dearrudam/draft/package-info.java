/// # Draft
/// > Speaker-owned talk drafts: private outlines polished before submission.
///
/// ## Boundary
/// - `list-drafts` — list the caller's drafts
/// - `create-draft` — create a draft for the caller
/// - `get-draft` — return one of the caller's drafts
/// - `update-draft` — update fields of the caller's draft
/// - `delete-draft` — delete the caller's draft
///
/// ## Requirements
///
/// ### R1 List drafts
/// - R1.1 — The capability shall return only the caller's drafts (200).
/// - R1.2 — The capability shall order the listing with the most recently created draft first.
///
/// ### R2 Create a draft
/// - R2.1 — When a speaker submits valid draft data, the capability shall store it owned by the caller and respond as created (201).
/// - R2.2 — If the draft input is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
///
/// ### R3 Get a draft
/// - R3.1 — While the draft exists and belongs to the caller, the capability shall return it (200).
/// - R3.2 — If the draft does not exist, then the capability shall respond that the draft was not found (404).
/// - R3.3 — If the draft belongs to another user, then the capability shall reject the request as forbidden (403).
///
/// ### R4 Update a draft
/// - R4.1 — While the draft belongs to the caller, when a partial update arrives, the capability shall apply only the provided fields and keep the others unchanged (200).
/// - R4.2 — If the draft does not exist, then the capability shall respond that the draft was not found (404).
/// - R4.3 — If the draft belongs to another user, then the capability shall reject the request as forbidden (403).
/// - R4.4 — If a provided field value is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
///
/// ### R5 Delete a draft
/// - R5.1 — While the draft belongs to the caller, the capability shall delete it and respond with no content (204).
/// - R5.2 — If the draft does not exist, then the capability shall respond that the draft was not found (404).
/// - R5.3 — If the draft belongs to another user, then the capability shall reject the request as forbidden (403).
///
/// ### R6 Speaker access
/// - R6.1 — The capability shall restrict every draft operation to authenticated speakers.
///
/// ## Entities
/// - Draft
///
/// ## Out of scope
/// - Submitting drafts as proposals (owned by the proposal capability)
/// - Talk scheduling and selection
package com.dearrudam.draft;
