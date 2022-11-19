package io.github.js.domain.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void 유저_생성_테스트() {
        // given
        String email = "email";
        Password password = Password.of("password", passwordEncoder);

        // when
        User user = User.of(email, password);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
    }

}