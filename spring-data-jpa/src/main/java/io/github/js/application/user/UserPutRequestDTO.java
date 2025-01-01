package io.github.js.application.user;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.js.domain.user.Email;
import io.github.js.domain.user.Password;
import io.github.js.domain.user.User;
import io.github.js.domain.user.UserName;
import lombok.Getter;

@JsonTypeName("user")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@Getter
public class UserPutRequestDTO {

    private final String email;
    private final String username;
    private final String password;

    public UserPutRequestDTO(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public User toEntity() {
        return User.of(
                new Email(email),
                new UserName(username),
                new Password(password));
    }

}
