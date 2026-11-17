package com.dearrudam.evaluation.control;

public abstract class EvaluationException extends RuntimeException {

    private final int status;

    protected EvaluationException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
