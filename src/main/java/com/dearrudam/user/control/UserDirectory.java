package com.dearrudam.user.control;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.dearrudam.user.entity.Role;
import com.dearrudam.user.entity.User;
import com.dearrudam.user.entity.UserRepository;

@ApplicationScoped
public class UserDirectory {

    @Inject
    UserRepository users;

    public List<User> list(Role role, String excludeEmail) {
        return users.withRole(role, excludeEmail);
    }

    public User get(Long id) {
        return Optional.ofNullable(users.findById(id))
                .orElseThrow(UserNotFoundException::new);
    }
}
