package io.github.js.application.user;

import io.github.js.domain.user.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static java.lang.String.valueOf;

@EqualsAndHashCode
@Getter
public class UserModel {

    private final Long id;
    private final String email;
    private final String nickname;

    private UserModel(Long id, String email, String nickname) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
    }

    public static UserModel of(User user) {
        return new UserModel(
                user.getId(),
                valueOf(user.getEmail()),
                valueOf(user.getNickname()));
    }
}
