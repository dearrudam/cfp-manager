package com.dearrudam.user.control;

import com.dearrudam.user.entity.User;

/// Outcome of a registration: the user and whether it was just created.
public record RegisteredUser(User user, boolean created) {
}
