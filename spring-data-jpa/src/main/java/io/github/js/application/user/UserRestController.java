package io.github.js.application.user;

import io.github.js.domain.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.http.ResponseEntity.*;

@RestController
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/users")
    public ResponseEntity<UserModel> postUser(@Valid @RequestBody UserPostRequestDTO dto) {
        UserModel user = userService.signUp(dto.toEntity());
        return created(URI.create("/users/" + user.getId())).body(user);
    }

    @GetMapping(value = "/users/{id}")
    public ResponseEntity<UserModel> getUser(@PathVariable Long id) {
        return of(userService.findById(id));
    }

    @PutMapping(value = "/users/{id}")
    public ResponseEntity<UserModel> putUser(@PathVariable Long id) {
        return null;
    }

    @DeleteMapping(value = "/users/{id}")
    public void deleteUser(@PathVariable Long id) {

    }

}
