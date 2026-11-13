package com.dearrudam.shared;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class TestDataCleaner {

    @Inject
    EntityManager em;

    public void clean() {
        em.createQuery("delete from Cfp").executeUpdate();
        em.createQuery("delete from Event").executeUpdate();
        em.createQuery("delete from User").executeUpdate();
    }
}
