package com.dearrudam.evaluation.entity;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import com.dearrudam.proposal.entity.Proposal;
import com.dearrudam.user.entity.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class EvaluationRepository implements PanacheRepository<Evaluation> {

    public Optional<Evaluation> find(Proposal proposal, User evaluator) {
        return find("proposal = ?1 and evaluator = ?2", proposal, evaluator).firstResultOptional();
    }
}
