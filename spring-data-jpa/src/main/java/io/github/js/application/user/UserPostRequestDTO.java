package io.github.js.application.user;

import io.github.js.domain.user.Email;
import io.github.js.domain.user.Nickname;
import io.github.js.domain.user.Password;
import io.github.js.domain.user.User;
import lombok.Getter;


import javax.validation.constraints.NotBlank;
import java.util.Objects;


@Getter
public class UserPostRequestDTO {

    @javax.validation.constraints.Email
    private final String email;
    @NotBlank
    private final String password;
    @NotBlank
    private final String nickname;

    public UserPostRequestDTO(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public User toEntity() {
        return User.of(
                new Email(email),
                new Nickname(nickname),
                new Password(password));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPostRequestDTO that = (UserPostRequestDTO) o;
        return Objects.equals(email, that.email) && Objects.equals(password, that.password) && Objects.equals(nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password, nickname);
    }
}
