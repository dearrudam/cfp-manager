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
public class UserRegistration {

    @Inject
    UserRepository users;

    @Transactional
    public RegisteredUser registerCoordinator(String name, String email, String password) {
        return register(name, email, password, null, Role.COORDINATOR);
    }

    @Transactional
    public RegisteredUser registerSpeaker(String name, String email, String password, String bio) {
        return register(name, email, password, bio, Role.SPEAKER);
    }

    /// Registers a user with the given role, or grants the role to an existing account.
    /// An existing account requires the matching password; it keeps its password hash and
    /// other roles, while name and bio (when provided) are updated.
    RegisteredUser register(String name, String email, String password, String bio, Role role) {
        var normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        var existing = users.findByEmail(normalizedEmail);
        if (existing.isEmpty()) {
            var user = new User();
            user.name = name;
            user.email = normalizedEmail;
            user.passwordHash = BcryptUtil.bcryptHash(password);
            user.bio = bio;
            user.roles.add(role);
            users.persist(user);
            return new RegisteredUser(user, true);
        }
        var user = existing.get();
        if (!BcryptUtil.matches(password, user.passwordHash)) {
            throw new InvalidCredentialsException();
        }
        user.name = name;
        if (bio != null) {
            user.bio = bio;
        }
        user.roles.add(role);
        return new RegisteredUser(user, false);
    }
}
