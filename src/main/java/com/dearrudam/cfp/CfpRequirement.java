package com.dearrudam.cfp;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [com.dearrudam.cfp] — do not edit.
/// Marks the test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CfpRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// When the event exists and has a CFP, the capability shall return its status and lifecycle timestamps (200).
        R1_1("R1.1", "When the event exists and has a CFP, the capability shall return its status and lifecycle timestamps (200)."),
        /// If the event does not exist, then the capability shall respond that the event was not found (404); if the event has no CFP, then the capability shall respond that the CFP was not found (404).
        R1_2("R1.2", "If the event does not exist, then the capability shall respond that the event was not found (404); if the event has no CFP, then the capability shall respond that the CFP was not found (404)."),
        /// The capability shall serve CFP reads without requiring authentication.
        R1_3("R1.3", "The capability shall serve CFP reads without requiring authentication."),
        /// When the event owner creates a CFP for an event that has none, the capability shall create it in closed state and respond as created (201).
        R2_1("R2.1", "When the event owner creates a CFP for an event that has none, the capability shall create it in closed state and respond as created (201)."),
        /// If the event already has a CFP, then the capability shall reject the creation as a conflict (409).
        R2_2("R2.2", "If the event already has a CFP, then the capability shall reject the creation as a conflict (409)."),
        /// If the event does not exist, then the capability shall respond that the event was not found (404).
        R2_3("R2.3", "If the event does not exist, then the capability shall respond that the event was not found (404)."),
        /// If the caller is not the event owner, then the capability shall reject the request as forbidden (403).
        R2_4("R2.4", "If the caller is not the event owner, then the capability shall reject the request as forbidden (403)."),
        /// If the caller is unauthenticated, then the capability shall reject the request (401).
        R2_5("R2.5", "If the caller is unauthenticated, then the capability shall reject the request (401)."),
        /// While the CFP is closed and has at least one committee member, when the owner opens it, the capability shall mark it open, record the opening instant, and clear the closing instant (200).
        R3_1("R3.1", "While the CFP is closed and has at least one committee member, when the owner opens it, the capability shall mark it open, record the opening instant, and clear the closing instant (200)."),
        /// If the CFP is already open, then the capability shall reject the request as a conflict (409).
        R3_2("R3.2", "If the CFP is already open, then the capability shall reject the request as a conflict (409)."),
        /// If the CFP has no committee members, then the capability shall reject the opening as a conflict (409).
        R3_3("R3.3", "If the CFP has no committee members, then the capability shall reject the opening as a conflict (409)."),
        /// If the event does not exist or has no CFP, then the capability shall respond not found (404).
        R3_4("R3.4", "If the event does not exist or has no CFP, then the capability shall respond not found (404)."),
        /// If the caller is not the event owner, then the capability shall reject the request as forbidden (403).
        R3_5("R3.5", "If the caller is not the event owner, then the capability shall reject the request as forbidden (403)."),
        /// If the caller is unauthenticated, then the capability shall reject the request (401).
        R3_6("R3.6", "If the caller is unauthenticated, then the capability shall reject the request (401)."),
        /// While the CFP is open, when the owner closes it, the capability shall mark it closed, record the closing instant, and move its submitted proposals to evaluation (200).
        R4_1("R4.1", "While the CFP is open, when the owner closes it, the capability shall mark it closed, record the closing instant, and move its submitted proposals to evaluation (200)."),
        /// If the CFP is already closed, then the capability shall reject the request as a conflict (409).
        R4_2("R4.2", "If the CFP is already closed, then the capability shall reject the request as a conflict (409)."),
        /// If the event does not exist or has no CFP, then the capability shall respond not found (404).
        R4_3("R4.3", "If the event does not exist or has no CFP, then the capability shall respond not found (404)."),
        /// If the caller is not the event owner, then the capability shall reject the request as forbidden (403).
        R4_4("R4.4", "If the caller is not the event owner, then the capability shall reject the request as forbidden (403)."),
        /// If the caller is unauthenticated, then the capability shall reject the request (401).
        R4_5("R4.5", "If the caller is unauthenticated, then the capability shall reject the request (401)."),
        /// The capability shall return the CFP's committee members (200).
        R5_1("R5.1", "The capability shall return the CFP's committee members (200)."),
        /// The capability shall serve committee reads without requiring authentication.
        R5_2("R5.2", "The capability shall serve committee reads without requiring authentication."),
        /// The capability shall order the listing by member name.
        R5_3("R5.3", "The capability shall order the listing by member name."),
        /// If the event does not exist or has no CFP, then the capability shall respond not found (404).
        R5_4("R5.4", "If the event does not exist or has no CFP, then the capability shall respond not found (404)."),
        /// When the owner adds an existing user, the capability shall record the membership and respond as created (201).
        R6_1("R6.1", "When the owner adds an existing user, the capability shall record the membership and respond as created (201)."),
        /// If the user is already a committee member, then the capability shall reject the request as a conflict (409).
        R6_2("R6.2", "If the user is already a committee member, then the capability shall reject the request as a conflict (409)."),
        /// If the user does not exist, then the capability shall respond that the user was not found (404).
        R6_3("R6.3", "If the user does not exist, then the capability shall respond that the user was not found (404)."),
        /// If the event does not exist or has no CFP, then the capability shall respond not found (404).
        R6_4("R6.4", "If the event does not exist or has no CFP, then the capability shall respond not found (404)."),
        /// If the caller is not the event owner, then the capability shall reject the request as forbidden (403).
        R6_5("R6.5", "If the caller is not the event owner, then the capability shall reject the request as forbidden (403)."),
        /// If the caller is unauthenticated, then the capability shall reject the request (401).
        R6_6("R6.6", "If the caller is unauthenticated, then the capability shall reject the request (401)."),
        /// When the owner removes a member and at least one other member remains, the capability shall delete the membership and respond with no content (204).
        R7_1("R7.1", "When the owner removes a member and at least one other member remains, the capability shall delete the membership and respond with no content (204)."),
        /// If the member does not belong to the committee, then the capability shall respond that the member was not found (404).
        R7_2("R7.2", "If the member does not belong to the committee, then the capability shall respond that the member was not found (404)."),
        /// If the member is the last one, then the capability shall reject the removal as a conflict (409).
        R7_3("R7.3", "If the member is the last one, then the capability shall reject the removal as a conflict (409)."),
        /// If the event does not exist or has no CFP, then the capability shall respond not found (404).
        R7_4("R7.4", "If the event does not exist or has no CFP, then the capability shall respond not found (404)."),
        /// If the caller is not the event owner, then the capability shall reject the request as forbidden (403).
        R7_5("R7.5", "If the caller is not the event owner, then the capability shall reject the request as forbidden (403)."),
        /// If the caller is unauthenticated, then the capability shall reject the request (401).
        R7_6("R7.6", "If the caller is unauthenticated, then the capability shall reject the request (401)."),
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
