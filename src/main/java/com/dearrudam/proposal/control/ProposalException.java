package com.dearrudam.proposal.control;

public abstract class ProposalException extends RuntimeException {

    private final int status;

    protected ProposalException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
