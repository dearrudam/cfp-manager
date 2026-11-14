package com.dearrudam.proposal.control;

public class EvaluationAccessDeniedException extends ProposalException {

    public EvaluationAccessDeniedException(String message) {
        super(403, message);
    }
}
