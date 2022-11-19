package io.github.js.application.user;

import io.github.js.domain.jwt.JWTSerializer;
import io.github.js.domain.user.UserService;
import io.github.js.infrastructure.jwt.UserJWTPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RestController
public class UserRestController {

    private final UserService userService;
    private final JWTSerializer jwtSerializer;

    @PostMapping(value = "/users")
    public UserModel postUser(@Valid @RequestBody UserPostRequestDTO dto) {
        final var user = userService.signUp(dto.getEmail(), dto.getPassword());
        return UserModel.of(user, jwtSerializer.jwtFromUser(user));
    }

    @PostMapping(value = "/users/login")
    public ResponseEntity<UserModel> loginUser(@Valid @RequestBody UserLoginRequestDTO dto) {
        final var user = userService.login(dto.getEmail(), dto.getPassword());
        return ok(UserModel.of(user, jwtSerializer.jwtFromUser(user)));
    }

    @GetMapping(value = "/user")
    public ResponseEntity<UserModel> getUser(@AuthenticationPrincipal UserJWTPayload jwtPayload) {
        return ok(UserModel.of(userService.findById(jwtPayload.getUserId()), getCurrentCredential()));
    }

    private static String getCurrentCredential() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getCredentials()
                .toString();
    }

}
