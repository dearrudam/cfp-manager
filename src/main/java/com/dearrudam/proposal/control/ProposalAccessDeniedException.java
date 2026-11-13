package com.dearrudam.proposal.control;

public class ProposalAccessDeniedException extends ProposalException {

    public ProposalAccessDeniedException() {
        super(403, "Only the proposal owner can manage this proposal");
    }
}
