package io.github.js.domain.user;

import io.github.js.application.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserModel signUp(User user) {
        User userSaved = userRepository.save(user);
        return UserModel.from(userSaved);
    }

    @Transactional(readOnly = true)
    public Optional<UserModel> findById(Long id) {
        return userRepository.findById(id).map(UserModel::from);
    }

    @Transactional
    public UserModel updateUser(Long id, User putUser) {
        final var findUser = userRepository.findById(id);
        findUser.ifPresent(user -> user.update(putUser));
        return UserModel.from(findUser.map(userRepository::save).orElseThrow());
    }

}
