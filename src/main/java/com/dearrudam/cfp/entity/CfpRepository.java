package com.dearrudam.cfp.entity;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class CfpRepository implements PanacheRepository<Cfp> {
}
