package com.fulfillflow.security;

import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UserService {
    private final UserRepository users;
    private final PasswordEncoder passwords;

    UserService(UserRepository users, PasswordEncoder passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    @Transactional
    UserResponse create(CreateUserRequest request) {
        var email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException(email);
        }
        var user = new AppUser(email, passwords.encode(request.password()), UserRole.valueOf(request.role()));
        return UserResponse.from(users.save(user));
    }
}
