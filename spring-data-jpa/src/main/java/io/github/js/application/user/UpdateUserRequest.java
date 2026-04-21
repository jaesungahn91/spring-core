package io.github.js.application.user;

import io.github.js.domain.user.Password;
import io.github.js.domain.user.User;
import io.github.js.domain.user.UserName;
import io.github.js.domain.user.Email;
import jakarta.validation.constraints.Size;

// @Email(Jakarta)과 domain Email 충돌로 @Email은 FQCN 사용
public record UpdateUserRequest(
        @jakarta.validation.constraints.Email String email,
        @Size(max = 50) String username,
        String password
) {
    public User toEntity() {
        return User.of(new Email(email), new UserName(username), new Password(password));
    }
}
