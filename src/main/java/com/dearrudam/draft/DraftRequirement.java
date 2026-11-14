package com.dearrudam.draft;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [com.dearrudam.draft] — do not edit.
/// Marks the test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DraftRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// The capability shall return only the caller's drafts (200).
        R1_1("R1.1", "The capability shall return only the caller's drafts (200)."),
        /// The capability shall order the listing with the most recently created draft first.
        R1_2("R1.2", "The capability shall order the listing with the most recently created draft first."),
        /// When a speaker submits valid draft data, the capability shall store it owned by the caller and respond as created (201).
        R2_1("R2.1", "When a speaker submits valid draft data, the capability shall store it owned by the caller and respond as created (201)."),
        /// If the draft input is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
        R2_2("R2.2", "If the draft input is invalid, then the capability shall reject it as a validation error identifying the offending field (400)."),
        /// While the draft exists and belongs to the caller, the capability shall return it (200).
        R3_1("R3.1", "While the draft exists and belongs to the caller, the capability shall return it (200)."),
        /// If the draft does not exist, then the capability shall respond that the draft was not found (404).
        R3_2("R3.2", "If the draft does not exist, then the capability shall respond that the draft was not found (404)."),
        /// If the draft belongs to another user, then the capability shall reject the request as forbidden (403).
        R3_3("R3.3", "If the draft belongs to another user, then the capability shall reject the request as forbidden (403)."),
        /// While the draft belongs to the caller, when a partial update arrives, the capability shall apply only the provided fields and keep the others unchanged (200).
        R4_1("R4.1", "While the draft belongs to the caller, when a partial update arrives, the capability shall apply only the provided fields and keep the others unchanged (200)."),
        /// If the draft does not exist, then the capability shall respond that the draft was not found (404).
        R4_2("R4.2", "If the draft does not exist, then the capability shall respond that the draft was not found (404)."),
        /// If the draft belongs to another user, then the capability shall reject the request as forbidden (403).
        R4_3("R4.3", "If the draft belongs to another user, then the capability shall reject the request as forbidden (403)."),
        /// If a provided field value is invalid, then the capability shall reject it as a validation error identifying the offending field (400).
        R4_4("R4.4", "If a provided field value is invalid, then the capability shall reject it as a validation error identifying the offending field (400)."),
        /// While the draft belongs to the caller, the capability shall delete it and respond with no content (204).
        R5_1("R5.1", "While the draft belongs to the caller, the capability shall delete it and respond with no content (204)."),
        /// If the draft does not exist, then the capability shall respond that the draft was not found (404).
        R5_2("R5.2", "If the draft does not exist, then the capability shall respond that the draft was not found (404)."),
        /// If the draft belongs to another user, then the capability shall reject the request as forbidden (403).
        R5_3("R5.3", "If the draft belongs to another user, then the capability shall reject the request as forbidden (403)."),
        /// The capability shall restrict every draft operation to authenticated speakers.
        R6_1("R6.1", "The capability shall restrict every draft operation to authenticated speakers."),
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
