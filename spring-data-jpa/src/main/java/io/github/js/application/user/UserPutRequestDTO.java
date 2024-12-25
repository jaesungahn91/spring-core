package io.github.js.application.user;

import lombok.Getter;

@Getter
public class UserPutRequestDTO {

    private final String email;
    private final String nickname;
    private final String password;

    public UserPutRequestDTO(String email, String nickname, String password) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }

}
