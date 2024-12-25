package io.github.js.application.user;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.js.domain.user.Email;
import io.github.js.domain.user.UserName;
import io.github.js.domain.user.Password;
import io.github.js.domain.user.User;
import lombok.Getter;


import javax.validation.constraints.NotBlank;

@JsonTypeName("user")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@Getter
public class UserPostRequestDTO {

    @javax.validation.constraints.Email
    private final String email;
    @NotBlank
    private final String password;
    @NotBlank
    private final String username;

    public UserPostRequestDTO(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }

    public User toEntity() {
        return User.of(
                new Email(email),
                new UserName(username),
                new Password(password));
    }

}
