package com.dearrudam.proposal.control;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.dearrudam.cfp.entity.CfpCommitteeRepository;
import com.dearrudam.proposal.entity.Proposal;
import com.dearrudam.proposal.entity.ProposalEvaluation;
import com.dearrudam.proposal.entity.ProposalEvaluationRepository;
import com.dearrudam.proposal.entity.ProposalSpeakerRepository;
import com.dearrudam.proposal.entity.ProposalStatus;
import com.dearrudam.user.control.InvalidCredentialsException;
import com.dearrudam.user.entity.User;
import com.dearrudam.user.entity.UserRepository;

@ApplicationScoped
public class EvaluationManagement {

    @Inject
    ProposalManagement proposals;

    @Inject
    ProposalEvaluationRepository evaluations;

    @Inject
    ProposalSpeakerRepository speakers;

    @Inject
    CfpCommitteeRepository committee;

    @Inject
    UserRepository users;

    public List<ProposalEvaluation> list(Long proposalId, String callerEmail) {
        var proposal = proposals.get(proposalId);
        var caller = caller(callerEmail);
        if (!canView(proposal, caller)) {
            throw new EvaluationAccessDeniedException(
                    "Only committee members or the event owner can view evaluations");
        }
        return evaluations.listByProposal(proposal);
    }

    @Transactional
    public ProposalEvaluation evaluate(Long proposalId, int score, String callerEmail) {
        var proposal = proposals.get(proposalId);
        var evaluator = caller(callerEmail);
        requireEvaluable(proposal);
        requireCommitteeMember(proposal, evaluator);
        requireNotAuthor(proposal, evaluator);
        requireNotAlreadyEvaluated(proposal, evaluator);
        var evaluation = new ProposalEvaluation();
        evaluation.proposal = proposal;
        evaluation.evaluator = evaluator;
        evaluation.score = score;
        evaluations.persist(evaluation);
        return evaluation;
    }

    User caller(String email) {
        return users.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
    }

    boolean canView(Proposal proposal, User user) {
        return proposal.cfp != null
                && (isCommitteeMember(proposal, user) || isEventOwner(proposal, user));
    }

    boolean isCommitteeMember(Proposal proposal, User user) {
        return committee.find(proposal.cfp, user).isPresent();
    }

    boolean isEventOwner(Proposal proposal, User user) {
        return proposal.cfp.event.owner.id.equals(user.id);
    }

    void requireEvaluable(Proposal proposal) {
        if (proposal.cfp == null || proposal.status != ProposalStatus.EVAL) {
            throw new InvalidProposalStateException("Proposal can only be evaluated while in EVAL status");
        }
    }

    void requireCommitteeMember(Proposal proposal, User evaluator) {
        if (!isCommitteeMember(proposal, evaluator)) {
            throw new EvaluationAccessDeniedException(
                    "Only committee members of this CFP can evaluate proposals");
        }
    }

    void requireNotAuthor(Proposal proposal, User evaluator) {
        if (evaluator.id.equals(proposal.owner.id) || speakers.find(proposal, evaluator).isPresent()) {
            throw new EvaluationAccessDeniedException("Authors cannot evaluate their own proposal");
        }
    }

    void requireNotAlreadyEvaluated(Proposal proposal, User evaluator) {
        if (evaluations.find(proposal, evaluator).isPresent()) {
            throw new EvaluationAlreadyExistsException();
        }
    }
}
