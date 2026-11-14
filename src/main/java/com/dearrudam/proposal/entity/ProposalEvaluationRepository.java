package com.dearrudam.proposal.entity;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import com.dearrudam.user.entity.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ProposalEvaluationRepository implements PanacheRepository<ProposalEvaluation> {

    public Optional<ProposalEvaluation> find(Proposal proposal, User evaluator) {
        return find("proposal = ?1 and evaluator = ?2", proposal, evaluator).firstResultOptional();
    }

    public List<ProposalEvaluation> listByProposal(Proposal proposal) {
        return list("proposal = ?1 order by createdAt", proposal);
    }
}
