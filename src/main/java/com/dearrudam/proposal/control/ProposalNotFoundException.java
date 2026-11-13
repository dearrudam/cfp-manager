package com.dearrudam.proposal.control;

public class ProposalNotFoundException extends ProposalException {

    public ProposalNotFoundException() {
        super(404, "Proposal not found");
    }
}
