package com.dearrudam.evaluation.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.dearrudam.cfp.entity.CfpCommitteeRepository;
import com.dearrudam.evaluation.entity.Evaluation;
import com.dearrudam.evaluation.entity.EvaluationRepository;
import com.dearrudam.proposal.control.ProposalManagement;
import com.dearrudam.proposal.entity.ProposalSpeakerRepository;
import com.dearrudam.proposal.entity.ProposalStatus;
import com.dearrudam.user.control.InvalidCredentialsException;
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
        var evaluator = users.findByEmail(callerEmail)
                .orElseThrow(InvalidCredentialsException::new);
        if (proposal.cfp == null || committee.find(proposal.cfp, evaluator).isEmpty()) {
            throw new EvaluationAccessDeniedException("Only committee members can evaluate proposals");
        }
        if (evaluator.id.equals(proposal.owner.id) || speakers.find(proposal, evaluator).isPresent()) {
            throw new EvaluationAccessDeniedException("Authors cannot evaluate their own proposal");
        }
        if (proposal.status != ProposalStatus.EVAL) {
            throw new InvalidEvaluationStateException("Proposal is not under evaluation");
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
}
