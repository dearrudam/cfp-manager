package com.dearrudam.review.entity;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import com.dearrudam.proposal.entity.Proposal;
import com.dearrudam.user.entity.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ReviewRepository implements PanacheRepository<Review> {

    public Optional<Review> find(Proposal proposal, User reviewer) {
        return find("proposal = ?1 and reviewer = ?2", proposal, reviewer).firstResultOptional();
    }

    public List<Review> listByProposal(Proposal proposal) {
        return list("proposal = ?1 order by createdAt", proposal);
    }
}
