package com.dearrudam.draft.control;

public abstract class DraftException extends RuntimeException {

    private final int status;

    protected DraftException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
