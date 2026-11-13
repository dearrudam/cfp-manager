package com.dearrudam.proposal.control;

public class InvalidProposalStateException extends ProposalException {

    public InvalidProposalStateException(String message) {
        super(409, message);
    }
}
