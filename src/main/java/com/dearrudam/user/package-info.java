/// # User
/// > Speaker and coordinator accounts: registration, directory listing, authentication, and profiles.
///
/// ## Boundary
/// - `register-speaker` — create a speaker account, or add the speaker role to an existing account
/// - `register-coordinator` — create a coordinator account, or add the coordinator role to an existing account
/// - `list-speakers` — list the users holding the speaker role
/// - `list-coordinators` — list the users holding the coordinator role
/// - `login` — exchange credentials for a bearer token
/// - `get-own-profile` — return the authenticated caller's profile
/// - `update-own-profile` — update the caller's name and bio
/// - `get-public-profile` — return a user's public profile by id
///
/// ## Requirements
///
/// ### R1 Register a speaker
/// - R1.1 — When a registration arrives with an email that has no account, the capability shall create an account holding the speaker role and respond as created (201).
/// - R1.2 — When a registration arrives for an existing account and the password matches, the capability shall add the speaker role to that account, update its name and, when provided, its bio, and respond with the account (200).
/// - R1.3 — If a registration arrives for an existing account and the password does not match, then the capability shall reject it as invalid credentials (401).
/// - R1.4 — If the registration input is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
/// - R1.5 — The capability shall normalize the account email by trimming whitespace and lowercasing it before storing or looking it up.
///
/// ### R2 Register a coordinator
/// - R2.1 — When a registration arrives with an email that has no account, the capability shall create an account holding the coordinator role and respond as created (201).
/// - R2.2 — When a registration arrives for an existing account and the password matches, the capability shall add the coordinator role to that account, update its name and, when provided, its bio, and respond with the account (200).
/// - R2.3 — If a registration arrives for an existing account and the password does not match, then the capability shall reject it as invalid credentials (401).
/// - R2.4 — If the registration input is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
/// - R2.5 — The capability shall normalize the account email by trimming whitespace and lowercasing it before storing or looking it up.
///
/// ### R3 List speakers
/// - R3.1 — The capability shall return every user holding the speaker role (200).
/// - R3.2 — When the caller is authenticated, the capability shall exclude the caller from the listing; when anonymous, it shall include every matching user.
/// - R3.3 — The capability shall expose only the public identity fields of each listed user.
/// - R3.4 — The capability shall order the listing by display name.
///
/// ### R4 List coordinators
/// - R4.1 — The capability shall return every user holding the coordinator role (200).
/// - R4.2 — When the caller is authenticated, the capability shall exclude the caller from the listing; when anonymous, it shall include every matching user.
/// - R4.3 — The capability shall expose only the public identity fields of each listed user.
/// - R4.4 — The capability shall order the listing by display name.
///
/// ### R5 Login
/// - R5.1 — When valid credentials are submitted, the capability shall respond with a bearer token, its type, and its lifetime (200).
/// - R5.2 — The issued token shall identify the account and carry its roles.
/// - R5.3 — If the password does not match or no account exists for the email, then the capability shall reject the login as invalid credentials (401).
/// - R5.4 — If the login input is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
///
/// ### R6 Get own profile
/// - R6.1 — While the caller is authenticated, the capability shall return the caller's profile including email, roles, bio when present, and creation timestamp (200).
/// - R6.2 — If the caller is unauthenticated or presents an invalid token, then the capability shall reject the request (401).
///
/// ### R7 Update own profile
/// - R7.1 — When a partial update arrives, the capability shall apply only the provided fields and keep the others unchanged (200).
/// - R7.2 — If a provided field value is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
/// - R7.3 — If the caller is unauthenticated or presents an invalid token, then the capability shall reject the request (401).
///
/// ### R8 Get public profile
/// - R8.1 — When an existing user id is requested, the capability shall return the user's public profile without exposing the account email (200).
/// - R8.2 — If the user has no bio, then the capability shall omit the bio from the response.
/// - R8.3 — If the user id does not exist, then the capability shall respond that the user was not found (404).
/// - R8.4 — The capability shall serve public profiles without requiring authentication.
///
/// ## Entities
/// - User
/// - Role
///
/// ## Out of scope
/// - Password reset, email verification, and account recovery
/// - Account deletion and administrative user management
/// - Token refresh and revocation
/// - Coordinator invitation or approval workflow
package com.dearrudam.user;
