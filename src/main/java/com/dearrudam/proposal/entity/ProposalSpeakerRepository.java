package com.dearrudam.proposal.entity;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import com.dearrudam.user.entity.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ProposalSpeakerRepository implements PanacheRepository<ProposalSpeaker> {

    public List<ProposalSpeaker> listByProposal(Proposal proposal) {
        return list("proposal = ?1 order by user.name", proposal);
    }

    public Optional<ProposalSpeaker> find(Proposal proposal, User user) {
        return find("proposal = ?1 and user = ?2", proposal, user).firstResultOptional();
    }
}
