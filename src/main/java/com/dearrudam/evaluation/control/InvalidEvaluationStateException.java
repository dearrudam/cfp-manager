package com.dearrudam.evaluation.control;

public class InvalidEvaluationStateException extends EvaluationException {

    public InvalidEvaluationStateException(String message) {
        super(409, message);
    }
}
