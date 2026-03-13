package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.User;
import io.github.js.batch.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserItemWriterTest {

    @Mock
    private UserRepository userRepository;

    private UserItemWriter writer;

    @BeforeEach
    void setUp() {
        writer = new UserItemWriter(userRepository);
    }

    @Test
    void write_insertsUser_whenEmailNotExists() throws Exception {
        User incoming = new User("new@example.com", "New User", 25);
        when(userRepository.findAllByEmailIn(List.of("new@example.com"))).thenReturn(List.of());

        writer.write(new Chunk<>(List.of(incoming)));

        verify(userRepository).saveAll(List.of(incoming));
    }

    @Test
    void write_updatesUser_whenEmailExists() throws Exception {
        User existing = new User("existing@example.com", "Old Name", 20);
        User incoming = new User("existing@example.com", "New Name", 30);
        when(userRepository.findAllByEmailIn(List.of("existing@example.com"))).thenReturn(List.of(existing));

        writer.write(new Chunk<>(List.of(incoming)));

        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getAge()).isEqualTo(30);
        verify(userRepository, never()).saveAll(any());
    }

    @Test
    void write_handlesChunk_withMixedInsertAndUpdate() throws Exception {
        User existingUser = new User("exists@example.com", "Old", 20);
        User incoming1 = new User("new@example.com", "New", 25);
        User incoming2 = new User("exists@example.com", "Updated", 35);

        when(userRepository.findAllByEmailIn(List.of("new@example.com", "exists@example.com")))
                .thenReturn(List.of(existingUser));

        writer.write(new Chunk<>(List.of(incoming1, incoming2)));

        verify(userRepository).saveAll(List.of(incoming1));
        assertThat(existingUser.getName()).isEqualTo("Updated");
    }

}