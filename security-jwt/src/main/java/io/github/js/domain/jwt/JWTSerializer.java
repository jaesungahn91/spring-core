package io.github.js.domain.jwt;

import io.github.js.domain.user.User;

public interface JWTSerializer {

    String jwtFromUser(User user);

}
