package com.fulfillflow.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class AdminBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final String email;
    private final String password;

    AdminBootstrap(
            UserRepository users,
            PasswordEncoder passwords,
            @Value("${fulfillflow.bootstrap.admin-email:}") String email,
            @Value("${fulfillflow.bootstrap.admin-password:}") String password) {
        this.users = users;
        this.passwords = passwords;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!email.isBlank() && !password.isBlank() && !users.existsByEmailIgnoreCase(email)) {
            users.save(new AppUser(email, passwords.encode(password), UserRole.ADMIN));
        }
    }
}
