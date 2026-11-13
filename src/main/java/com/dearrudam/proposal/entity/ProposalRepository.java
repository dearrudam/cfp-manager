package com.dearrudam.proposal.entity;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ProposalRepository implements PanacheRepository<Proposal> {

    public List<Proposal> listByOwnerEmail(String email) {
        return list("owner.email = ?1 order by createdAt desc", email);
    }
}
