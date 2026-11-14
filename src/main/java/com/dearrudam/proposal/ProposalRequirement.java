package com.dearrudam.proposal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [com.dearrudam.proposal] — do not edit.
/// Marks the test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProposalRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// When a speaker submits valid proposal data for an event whose CFP is open, the capability shall store it as submitted and respond as created (201).
        R1_1("R1.1", "When a speaker submits valid proposal data for an event whose CFP is open, the capability shall store it as submitted and respond as created (201)."),
        /// If the submission requests a draft copy, then the capability shall also keep a draft with the same content for the speaker.
        R1_2("R1.2", "If the submission requests a draft copy, then the capability shall also keep a draft with the same content for the speaker."),
        /// If the event does not exist, then the capability shall respond that the event was not found (404); if the event has no CFP, then the capability shall respond that the CFP was not found (404); if the CFP is not open, then the capability shall reject the submission as a conflict (409).
        R1_3("R1.3", "If the event does not exist, then the capability shall respond that the event was not found (404); if the event has no CFP, then the capability shall respond that the CFP was not found (404); if the CFP is not open, then the capability shall reject the submission as a conflict (409)."),
        /// If the proposal input is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
        R1_4("R1.4", "If the proposal input is invalid, then the capability shall reject it as a validation error identifying the offending field (400)."),
        /// If the caller is not an authenticated speaker, then the capability shall reject the request.
        R1_5("R1.5", "If the caller is not an authenticated speaker, then the capability shall reject the request."),
        /// The capability shall return only the caller's proposals (200).
        R2_1("R2.1", "The capability shall return only the caller's proposals (200)."),
        /// If the caller is not an authenticated speaker, then the capability shall reject the request.
        R2_2("R2.2", "If the caller is not an authenticated speaker, then the capability shall reject the request."),
        /// While the proposal exists, the capability shall return it including its co-speakers to any authenticated caller (200).
        R3_1("R3.1", "While the proposal exists, the capability shall return it including its co-speakers to any authenticated caller (200)."),
        /// If the caller is unauthenticated, then the capability shall reject the request (401).
        R3_2("R3.2", "If the caller is unauthenticated, then the capability shall reject the request (401)."),
        /// If the proposal does not exist, then the capability shall respond that the proposal was not found (404).
        R3_3("R3.3", "If the proposal does not exist, then the capability shall respond that the proposal was not found (404)."),
        /// While the caller owns the proposal and its CFP is open, the capability shall replace its title, abstract, level, and duration and keep its status (200).
        R4_1("R4.1", "While the caller owns the proposal and its CFP is open, the capability shall replace its title, abstract, level, and duration and keep its status (200)."),
        /// If the caller does not own the proposal, then the capability shall reject the request as forbidden (403).
        R4_2("R4.2", "If the caller does not own the proposal, then the capability shall reject the request as forbidden (403)."),
        /// If the proposal's CFP is not open, then the capability shall reject the update as a conflict (409).
        R4_3("R4.3", "If the proposal's CFP is not open, then the capability shall reject the update as a conflict (409)."),
        /// If the proposal does not exist, then the capability shall respond that the proposal was not found (404).
        R4_4("R4.4", "If the proposal does not exist, then the capability shall respond that the proposal was not found (404)."),
        /// If the caller is not an authenticated speaker, then the capability shall reject the request.
        R4_5("R4.5", "If the caller is not an authenticated speaker, then the capability shall reject the request."),
        /// If the update input is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
        R4_6("R4.6", "If the update input is invalid, then the capability shall reject it as a validation error identifying the offending field (400)."),
        /// While the caller owns the proposal and its CFP is open, when an existing user is added, the capability shall record the co-speaker and respond as created (201).
        R5_1("R5.1", "While the caller owns the proposal and its CFP is open, when an existing user is added, the capability shall record the co-speaker and respond as created (201)."),
        /// If the user is already a co-speaker, then the capability shall reject the request as a conflict (409).
        R5_2("R5.2", "If the user is already a co-speaker, then the capability shall reject the request as a conflict (409)."),
        /// If the user is the proposal owner, then the capability shall reject the request as a conflict (409).
        R5_3("R5.3", "If the user is the proposal owner, then the capability shall reject the request as a conflict (409)."),
        /// If the user does not exist, then the capability shall respond that the user was not found (404).
        R5_4("R5.4", "If the user does not exist, then the capability shall respond that the user was not found (404)."),
        /// If the proposal does not exist, then the capability shall respond that the proposal was not found (404).
        R5_5("R5.5", "If the proposal does not exist, then the capability shall respond that the proposal was not found (404)."),
        /// If the caller does not own the proposal, then the capability shall reject the request as forbidden (403).
        R5_6("R5.6", "If the caller does not own the proposal, then the capability shall reject the request as forbidden (403)."),
        /// If the proposal's CFP is not open, then the capability shall reject the request as a conflict (409).
        R5_7("R5.7", "If the proposal's CFP is not open, then the capability shall reject the request as a conflict (409)."),
        /// If the caller is not an authenticated speaker, then the capability shall reject the request.
        R5_8("R5.8", "If the caller is not an authenticated speaker, then the capability shall reject the request."),
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
