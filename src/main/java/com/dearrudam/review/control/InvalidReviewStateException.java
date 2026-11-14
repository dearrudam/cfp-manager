package com.dearrudam.review.control;

public class InvalidReviewStateException extends ReviewException {

    public InvalidReviewStateException(String message) {
        super(409, message);
    }
}
