package com.dearrudam.draft.entity;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class DraftRepository implements PanacheRepository<Draft> {

    public List<Draft> listByOwnerEmail(String email) {
        return list("owner.email = ?1 order by createdAt desc", email);
    }
}
