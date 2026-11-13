package com.dearrudam.user.control;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.dearrudam.user.entity.User;
import com.dearrudam.user.entity.UserRepository;

@ApplicationScoped
public class UserProfile {

    @Inject
    UserRepository users;

    public User get(String email) {
        return users.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
    }

    @Transactional
    public User update(String email, Optional<String> name, Optional<String> bio) {
        var user = get(email);
        name.ifPresent(n -> user.name = n);
        bio.ifPresent(b -> user.bio = b);
        return user;
    }
}
