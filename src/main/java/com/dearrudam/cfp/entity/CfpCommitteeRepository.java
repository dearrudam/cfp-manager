package com.dearrudam.cfp.entity;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import com.dearrudam.user.entity.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class CfpCommitteeRepository implements PanacheRepository<CfpCommitteeMember> {

    public Optional<CfpCommitteeMember> find(Cfp cfp, User user) {
        return find("cfp = ?1 and user = ?2", cfp, user).firstResultOptional();
    }

    public Optional<CfpCommitteeMember> findByUserId(Cfp cfp, Long userId) {
        return find("cfp = ?1 and user.id = ?2", cfp, userId).firstResultOptional();
    }
}
