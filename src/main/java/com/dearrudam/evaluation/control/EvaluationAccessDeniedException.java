package com.dearrudam.evaluation.control;

public class EvaluationAccessDeniedException extends EvaluationException {

    public EvaluationAccessDeniedException(String message) {
        super(403, message);
    }
}
