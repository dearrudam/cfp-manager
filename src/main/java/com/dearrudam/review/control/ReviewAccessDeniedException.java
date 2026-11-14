package com.dearrudam.review.control;

public class ReviewAccessDeniedException extends ReviewException {

    public ReviewAccessDeniedException(String message) {
        super(403, message);
    }
}
