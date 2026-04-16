package io.github.js.application.user;

import io.github.js.domain.user.Email;
import io.github.js.domain.user.Password;
import io.github.js.domain.user.User;
import io.github.js.domain.user.UserName;

public record UpdateUserRequest(
        String email,
        String username,
        String password
) {
    public User toEntity() {
        return User.of(new Email(email), new UserName(username), new Password(password));
    }
}
