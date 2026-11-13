package com.dearrudam.proposal.control;

import com.dearrudam.draft.entity.Draft;
import com.dearrudam.proposal.entity.Proposal;

public record CreatedProposal(Proposal proposal, Draft draft) {
}
