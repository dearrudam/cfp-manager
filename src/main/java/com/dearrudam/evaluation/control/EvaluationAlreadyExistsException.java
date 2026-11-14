package com.dearrudam.evaluation.control;

public class EvaluationAlreadyExistsException extends EvaluationException {

    public EvaluationAlreadyExistsException() {
        super(409, "Evaluator already submitted an evaluation for this proposal");
    }
}
