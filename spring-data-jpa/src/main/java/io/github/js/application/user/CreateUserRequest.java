package io.github.js.application.user;

import io.github.js.domain.user.Email;
import io.github.js.domain.user.Password;
import io.github.js.domain.user.User;
import io.github.js.domain.user.UserName;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @jakarta.validation.constraints.Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String username
) {
    public User toEntity() {
        return User.of(new Email(email), new UserName(username), new Password(password));
    }
}
