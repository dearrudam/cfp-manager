package com.dearrudam.user.entity;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public List<User> withRole(Role role, String excludeEmail) {
        var query = "select u from User u join u.roles r where r = ?1";
        return excludeEmail == null
                ? list(query + " order by u.name", role)
                : list(query + " and u.email <> ?2 order by u.name", role, excludeEmail);
    }
}
