package com.dearrudam.evaluation.control;

public class EvaluationAlreadyExistsException extends EvaluationException {

    public EvaluationAlreadyExistsException() {
        super(409, "User already evaluated this proposal");
    }
}
