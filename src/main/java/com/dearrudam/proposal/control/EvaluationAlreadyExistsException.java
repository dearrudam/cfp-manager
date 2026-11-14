package com.dearrudam.proposal.control;

public class EvaluationAlreadyExistsException extends ProposalException {

    public EvaluationAlreadyExistsException() {
        super(409, "Evaluator has already evaluated this proposal");
    }
}
