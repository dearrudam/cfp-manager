package com.dearrudam.review.control;

public abstract class ReviewException extends RuntimeException {

    private final int status;

    protected ReviewException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
