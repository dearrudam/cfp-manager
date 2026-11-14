package com.dearrudam.user;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [com.dearrudam.user] — do not edit.
/// Marks the test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// When a registration arrives with an email that has no account, the capability shall create an account holding the speaker role and respond as created (201).
        R1_1("R1.1", "When a registration arrives with an email that has no account, the capability shall create an account holding the speaker role and respond as created (201)."),
        /// When a registration arrives for an existing account and the password matches, the capability shall add the speaker role to that account, update its name and, when provided, its bio, and respond with the account (200).
        R1_2("R1.2", "When a registration arrives for an existing account and the password matches, the capability shall add the speaker role to that account, update its name and, when provided, its bio, and respond with the account (200)."),
        /// If a registration arrives for an existing account and the password does not match, then the capability shall reject it as invalid credentials (401).
        R1_3("R1.3", "If a registration arrives for an existing account and the password does not match, then the capability shall reject it as invalid credentials (401)."),
        /// If the registration input is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
        R1_4("R1.4", "If the registration input is invalid, then the capability shall reject it as a validation error identifying the offending field (400)."),
        /// The capability shall normalize the account email by trimming whitespace and lowercasing it before storing or looking it up.
        R1_5("R1.5", "The capability shall normalize the account email by trimming whitespace and lowercasing it before storing or looking it up."),
        /// When a registration arrives with an email that has no account, the capability shall create an account holding the coordinator role and respond as created (201).
        R2_1("R2.1", "When a registration arrives with an email that has no account, the capability shall create an account holding the coordinator role and respond as created (201)."),
        /// When a registration arrives for an existing account and the password matches, the capability shall add the coordinator role to that account, update its name and, when provided, its bio, and respond with the account (200).
        R2_2("R2.2", "When a registration arrives for an existing account and the password matches, the capability shall add the coordinator role to that account, update its name and, when provided, its bio, and respond with the account (200)."),
        /// If a registration arrives for an existing account and the password does not match, then the capability shall reject it as invalid credentials (401).
        R2_3("R2.3", "If a registration arrives for an existing account and the password does not match, then the capability shall reject it as invalid credentials (401)."),
        /// If the registration input is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
        R2_4("R2.4", "If the registration input is invalid, then the capability shall reject it as a validation error identifying the offending field (400)."),
        /// The capability shall normalize the account email by trimming whitespace and lowercasing it before storing or looking it up.
        R2_5("R2.5", "The capability shall normalize the account email by trimming whitespace and lowercasing it before storing or looking it up."),
        /// The capability shall return every user holding the speaker role (200).
        R3_1("R3.1", "The capability shall return every user holding the speaker role (200)."),
        /// When the caller is authenticated, the capability shall exclude the caller from the listing; when anonymous, it shall include every matching user.
        R3_2("R3.2", "When the caller is authenticated, the capability shall exclude the caller from the listing; when anonymous, it shall include every matching user."),
        /// The capability shall expose only the public identity fields of each listed user.
        R3_3("R3.3", "The capability shall expose only the public identity fields of each listed user."),
        /// The capability shall order the listing by display name.
        R3_4("R3.4", "The capability shall order the listing by display name."),
        /// The capability shall return every user holding the coordinator role (200).
        R4_1("R4.1", "The capability shall return every user holding the coordinator role (200)."),
        /// When the caller is authenticated, the capability shall exclude the caller from the listing; when anonymous, it shall include every matching user.
        R4_2("R4.2", "When the caller is authenticated, the capability shall exclude the caller from the listing; when anonymous, it shall include every matching user."),
        /// The capability shall expose only the public identity fields of each listed user.
        R4_3("R4.3", "The capability shall expose only the public identity fields of each listed user."),
        /// The capability shall order the listing by display name.
        R4_4("R4.4", "The capability shall order the listing by display name."),
        /// When valid credentials are submitted, the capability shall respond with a bearer token, its type, and its lifetime (200).
        R5_1("R5.1", "When valid credentials are submitted, the capability shall respond with a bearer token, its type, and its lifetime (200)."),
        /// The issued token shall identify the account and carry its roles.
        R5_2("R5.2", "The issued token shall identify the account and carry its roles."),
        /// If the password does not match or no account exists for the email, then the capability shall reject the login as invalid credentials (401).
        R5_3("R5.3", "If the password does not match or no account exists for the email, then the capability shall reject the login as invalid credentials (401)."),
        /// If the login input is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
        R5_4("R5.4", "If the login input is invalid, then the capability shall reject it as a validation error identifying the offending field (400)."),
        /// While the caller is authenticated, the capability shall return the caller's profile including email, roles, bio when present, and creation timestamp (200).
        R6_1("R6.1", "While the caller is authenticated, the capability shall return the caller's profile including email, roles, bio when present, and creation timestamp (200)."),
        /// If the caller is unauthenticated or presents an invalid token, then the capability shall reject the request (401).
        R6_2("R6.2", "If the caller is unauthenticated or presents an invalid token, then the capability shall reject the request (401)."),
        /// When a partial update arrives, the capability shall apply only the provided fields and keep the others unchanged (200).
        R7_1("R7.1", "When a partial update arrives, the capability shall apply only the provided fields and keep the others unchanged (200)."),
        /// If a provided field value is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
        R7_2("R7.2", "If a provided field value is invalid, then the capability shall reject it as a validation error identifying the offending field (400)."),
        /// If the caller is unauthenticated or presents an invalid token, then the capability shall reject the request (401).
        R7_3("R7.3", "If the caller is unauthenticated or presents an invalid token, then the capability shall reject the request (401)."),
        /// When an existing user id is requested, the capability shall return the user's public profile without exposing the account email (200).
        R8_1("R8.1", "When an existing user id is requested, the capability shall return the user's public profile without exposing the account email (200)."),
        /// If the user has no bio, then the capability shall omit the bio from the response.
        R8_2("R8.2", "If the user has no bio, then the capability shall omit the bio from the response."),
        /// If the user id does not exist, then the capability shall respond that the user was not found (404).
        R8_3("R8.3", "If the user id does not exist, then the capability shall respond that the user was not found (404)."),
        /// The capability shall serve public profiles without requiring authentication.
        R8_4("R8.4", "The capability shall serve public profiles without requiring authentication."),
        ;

        private final String id;
        private final String statement;

        Rn(String id, String statement) {
            this.id = id;
            this.statement = statement;
        }

        public String statement() {
            return statement;
        }

        @Override
        public String toString() {
            return id;
        }
    }

    Rn[] value();
}
