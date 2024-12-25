package io.github.js.application.user;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.js.domain.user.User;
import lombok.Getter;

import static java.lang.String.valueOf;

@JsonTypeName("user")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@Getter
public class UserModel {

    private final Long id;
    private final String email;
    private final String username;
    private final String bio;
    private final String image;

    private UserModel(Long id, String email, String username, String bio, String image) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.bio = bio;
        this.image = image;
    }

    public static UserModel from(User user) {
        return new UserModel(
                user.getId(),
                valueOf(user.getEmail()),
                valueOf(user.getUserName()),
                "",
                "");
    }

}
