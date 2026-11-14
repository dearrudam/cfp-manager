package com.dearrudam.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [com.dearrudam.event] — do not edit.
/// Marks the test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// The capability shall return every registered event (200).
        R1_1("R1.1", "The capability shall return every registered event (200)."),
        /// The capability shall order the listing by scheduled start.
        R1_2("R1.2", "The capability shall order the listing by scheduled start."),
        /// The capability shall serve event listings without requiring authentication.
        R1_3("R1.3", "The capability shall serve event listings without requiring authentication."),
        /// When an existing event id is requested, the capability shall return the event including its owner identity (200).
        R2_1("R2.1", "When an existing event id is requested, the capability shall return the event including its owner identity (200)."),
        /// If the event id does not exist, then the capability shall respond that the event was not found (404).
        R2_2("R2.2", "If the event id does not exist, then the capability shall respond that the event was not found (404)."),
        /// The capability shall serve event reads without requiring authentication.
        R2_3("R2.3", "The capability shall serve event reads without requiring authentication."),
        /// When an authenticated coordinator submits valid event data, the capability shall create the event owned by the caller and respond as created (201).
        R3_1("R3.1", "When an authenticated coordinator submits valid event data, the capability shall create the event owned by the caller and respond as created (201)."),
        /// If the end instant is not after the start instant, then the capability shall reject the request (400).
        R3_2("R3.2", "If the end instant is not after the start instant, then the capability shall reject the request (400)."),
        /// If the event input is invalid, then the capability shall reject it as a validation error (400).
        R3_3("R3.3", "If the event input is invalid, then the capability shall reject it as a validation error (400)."),
        /// If the caller is not an authenticated coordinator, then the capability shall reject the request.
        R3_4("R3.4", "If the caller is not an authenticated coordinator, then the capability shall reject the request."),
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
