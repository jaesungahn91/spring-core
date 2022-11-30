package io.github.js.application.user;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.js.domain.user.User;
import lombok.Getter;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_OBJECT;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeName("user")
@JsonTypeInfo(include = WRAPPER_OBJECT, use = NAME)
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
