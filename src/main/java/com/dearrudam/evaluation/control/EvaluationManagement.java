package com.dearrudam.evaluation.control;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.dearrudam.cfp.entity.CfpCommitteeRepository;
import com.dearrudam.evaluation.entity.Evaluation;
import com.dearrudam.evaluation.entity.EvaluationRepository;
import com.dearrudam.proposal.control.InvalidProposalStateException;
import com.dearrudam.proposal.control.ProposalManagement;
import com.dearrudam.proposal.entity.Proposal;
import com.dearrudam.proposal.entity.ProposalSpeakerRepository;
import com.dearrudam.proposal.entity.ProposalStatus;
import com.dearrudam.user.control.InvalidCredentialsException;
import com.dearrudam.user.entity.User;
import com.dearrudam.user.entity.UserRepository;

@ApplicationScoped
public class EvaluationManagement {

    @Inject
    EvaluationRepository evaluations;

    @Inject
    ProposalManagement proposals;

    @Inject
    ProposalSpeakerRepository speakers;

    @Inject
    CfpCommitteeRepository committee;

    @Inject
    UserRepository users;

    @Transactional
    public Evaluation submit(Long proposalId, Integer score, String callerEmail) {
        var proposal = proposals.get(proposalId);
        var evaluator = caller(callerEmail);
        requireCommitteeMember(proposal, evaluator);
        requireNotAuthor(proposal, evaluator);
        if (proposal.status != ProposalStatus.EVAL) {
            throw new InvalidProposalStateException("Proposal can only be evaluated while in EVAL status");
        }
        if (evaluations.find(proposal, evaluator).isPresent()) {
            throw new EvaluationAlreadyExistsException();
        }
        var evaluation = new Evaluation();
        evaluation.proposal = proposal;
        evaluation.evaluator = evaluator;
        evaluation.score = score;
        evaluations.persist(evaluation);
        return evaluation;
    }

    public List<Evaluation> list(Long proposalId, String callerEmail) {
        var proposal = proposals.get(proposalId);
        requireCommitteeMember(proposal, caller(callerEmail));
        return evaluations.listByProposal(proposal);
    }

    private User caller(String callerEmail) {
        return users.findByEmail(callerEmail)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private void requireCommitteeMember(Proposal proposal, User user) {
        var member = proposal.cfp != null
                && committee.findByUserId(proposal.cfp, user.id).isPresent();
        if (!member) {
            throw new EvaluationAccessDeniedException(
                    "Only committee members can access this proposal's evaluations");
        }
    }

    private void requireNotAuthor(Proposal proposal, User user) {
        if (proposal.owner.id.equals(user.id) || speakers.find(proposal, user).isPresent()) {
            throw new EvaluationAccessDeniedException("Authors cannot evaluate their own proposal");
        }
    }
}
