/// # Event
/// > Tech events that host calls for papers.
///
/// ## Boundary
/// - `list-events` — list every registered event
/// - `get-event` — return an event by id
/// - `create-event` — register a new event owned by the caller
///
/// ## Requirements
///
/// ### R1 List events
/// - R1.1 — The capability shall return every registered event (200).
/// - R1.2 — The capability shall order the listing by scheduled start.
/// - R1.3 — The capability shall serve event listings without requiring authentication.
///
/// ### R2 Get an event
/// - R2.1 — When an existing event id is requested, the capability shall return the event including its owner identity (200).
/// - R2.2 — If the event id does not exist, then the capability shall respond that the event was not found (404).
/// - R2.3 — The capability shall serve event reads without requiring authentication.
///
/// ### R3 Create an event
/// - R3.1 — When an authenticated coordinator submits valid event data, the capability shall create the event owned by the caller and respond as created (201).
/// - R3.2 — If the end instant is not after the start instant, then the capability shall reject the request (400).
/// - R3.3 — If the event input is invalid, then the capability shall reject it as a validation error (400).
/// - R3.4 — If the caller is not an authenticated coordinator, then the capability shall reject the request.
///
/// ## Entities
/// - Event
///
/// ## Out of scope
/// - Updating, cancelling, or deleting events
/// - CFP lifecycle (owned by the cfp capability)
/// - Proposal submission and triage
package com.dearrudam.event;
