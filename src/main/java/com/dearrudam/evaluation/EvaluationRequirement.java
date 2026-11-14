package com.dearrudam.evaluation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [com.dearrudam.evaluation] — do not edit.
/// Marks the test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EvaluationRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// While the caller is an authenticated committee member of the proposal's CFP, the proposal is under evaluation, the caller is neither its author nor a co-author, and the caller has not evaluated it yet, when a score between 1 and 5 is submitted, the capability shall record the evaluation and respond as created (201).
        R1_1("R1.1", "While the caller is an authenticated committee member of the proposal's CFP, the proposal is under evaluation, the caller is neither its author nor a co-author, and the caller has not evaluated it yet, when a score between 1 and 5 is submitted, the capability shall record the evaluation and respond as created (201)."),
        /// If the caller is unauthenticated, then the capability shall reject the request (401).
        R1_2("R1.2", "If the caller is unauthenticated, then the capability shall reject the request (401)."),
        /// If the caller is not a committee member of the proposal's CFP, then the capability shall reject the request as forbidden (403).
        R1_3("R1.3", "If the caller is not a committee member of the proposal's CFP, then the capability shall reject the request as forbidden (403)."),
        /// If the caller is the proposal's author or one of its co-authors, then the capability shall reject the request as forbidden (403).
        R1_4("R1.4", "If the caller is the proposal's author or one of its co-authors, then the capability shall reject the request as forbidden (403)."),
        /// If the proposal is not under evaluation, then the capability shall reject the request as a conflict (409).
        R1_5("R1.5", "If the proposal is not under evaluation, then the capability shall reject the request as a conflict (409)."),
        /// If the caller has already evaluated the proposal, then the capability shall reject the request as a conflict (409).
        R1_6("R1.6", "If the caller has already evaluated the proposal, then the capability shall reject the request as a conflict (409)."),
        /// If the proposal does not exist, then the capability shall respond that the proposal was not found (404).
        R1_7("R1.7", "If the proposal does not exist, then the capability shall respond that the proposal was not found (404)."),
        /// If the score is missing or outside the range 1 to 5, then the capability shall reject it as a validation error identifying the offending field (400).
        R1_8("R1.8", "If the score is missing or outside the range 1 to 5, then the capability shall reject it as a validation error identifying the offending field (400)."),
        /// The capability shall not alter or remove a recorded evaluation.
        R1_9("R1.9", "The capability shall not alter or remove a recorded evaluation."),
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
