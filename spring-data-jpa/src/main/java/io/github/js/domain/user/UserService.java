package io.github.js.domain.user;

import io.github.js.application.user.UserModel;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserModel signUp(User user) {
        User userSaved = userRepository.save(user);
        return UserModel.of(userSaved);
    }

    public Optional<UserModel> findById(Long id) {
        return userRepository.findById(id).map(UserModel::of);
    }
}
