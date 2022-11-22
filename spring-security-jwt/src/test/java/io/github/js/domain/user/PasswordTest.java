package io.github.js.domain.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void 암호_생성_테스트() {
        // given
        String rawPassword = "raw-password";
        String encodedPassword = "encoded-password";

        // when
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        Password password = Password.of(rawPassword, passwordEncoder);

        // then
        verify(passwordEncoder, times(1)).encode(rawPassword);
        assertThat(password.getEncodedPassword()).isEqualTo(encodedPassword);
    }


    @Test
    void 암호_일치_테스트() {
        // given
        String rawPassword = "raw-password";
        String encodedPassword = "encoded-password";
        given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);

        // when
        Password password = Password.of(rawPassword, passwordEncoder);
        when(password.matchesPassword(rawPassword, passwordEncoder)).thenReturn(true);
        boolean actual = password.matchesPassword(rawPassword, passwordEncoder);

        // then
        then(passwordEncoder).should(times(1)).matches(rawPassword, encodedPassword);
        assertThat(actual).isTrue();
    }
}