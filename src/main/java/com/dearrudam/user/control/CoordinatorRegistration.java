package com.dearrudam.user.control;

import java.util.Locale;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.dearrudam.user.entity.Role;
import com.dearrudam.user.entity.User;
import com.dearrudam.user.entity.UserRepository;

import io.quarkus.elytron.security.common.BcryptUtil;

@ApplicationScoped
public class CoordinatorRegistration {

    @Inject
    UserRepository users;

    @Transactional
    public User register(String name, String email, String password) {
        var normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException(normalizedEmail);
        }
        var user = new User();
        user.name = name;
        user.email = normalizedEmail;
        user.passwordHash = BcryptUtil.bcryptHash(password);
        user.roles.add(Role.COORDINATOR);
        users.persist(user);
        return user;
    }
}
