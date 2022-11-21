package io.github.js.domain.user;

import io.github.js.application.user.UserPostRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public User signUp(String email, String rawPassword) {
        final var encodedPassword = Password.of(rawPassword, passwordEncoder);
        return userRepository.save(User.of(email, encodedPassword));
    }

    public User login(String email, String rawPassword) {
        return userRepository.findFirstByEmail(email)
                .filter(user -> user.matchesPassword(rawPassword, passwordEncoder))
                .orElseThrow(NoSuchElementException::new);
    }

    public User findById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(NoSuchElementException::new);
    }
}
