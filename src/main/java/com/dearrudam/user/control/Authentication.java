package com.dearrudam.user.control;

import java.time.Duration;
import java.util.Locale;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.dearrudam.user.entity.Role;
import com.dearrudam.user.entity.User;
import com.dearrudam.user.entity.UserRepository;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;

@ApplicationScoped
public class Authentication {

    static final String ISSUER = "c4p-manager";

    @Inject
    UserRepository users;

    @ConfigProperty(name = "jwt.token-ttl-seconds")
    long tokenTtlSeconds;

    public IssuedToken authenticate(String email, String password) {
        var normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        var user = users.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);
        if (!BcryptUtil.matches(password, user.passwordHash)) {
            throw new InvalidCredentialsException();
        }
        return new IssuedToken(issueToken(user), tokenTtlSeconds);
    }

    String issueToken(User user) {
        return Jwt.claims()
                .issuer(ISSUER)
                .subject(String.valueOf(user.id))
                .upn(user.email)
                .claim("name", user.name)
                .groups(user.roles.stream().map(Role::name).collect(Collectors.toSet()))
                .expiresIn(Duration.ofSeconds(tokenTtlSeconds))
                .sign();
    }
}
