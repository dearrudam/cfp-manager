package com.dearrudam.proposal.entity;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ProposalRepository implements PanacheRepository<Proposal> {

    public List<Proposal> listBySpeakerEmail(String email) {
        return list("speaker.email = ?1 order by submittedAt desc", email);
    }
}
