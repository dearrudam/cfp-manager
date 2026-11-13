package com.dearrudam.cfp.control;

public abstract class CfpException extends RuntimeException {

    private final int status;

    protected CfpException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
