package io.github.js.application.user;

import io.github.js.domain.user.User;
import lombok.Getter;

@Getter
public class UserModel {

    String email;
    String token;

    public static UserModel of(User user, String token) {
        return new UserModel(user.getEmail(), token);
    }

    private UserModel(String email, String token) {
        this.email = email;
        this.token = token;
    }

    protected UserModel() {
    }

}
