package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserItemProcessorTest {

    private UserItemProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new UserItemProcessor();
    }

    @Test
    void validUser_returnsUser() throws Exception {
        UserImportDto dto = dto("alice@example.com", "Alice", 25);
        User result = processor.process(dto);
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void invalidEmail_returnsNull() throws Exception {
        assertThat(processor.process(dto("not-an-email", "Bob", 30))).isNull();
        assertThat(processor.process(dto("missing@dot", "Bob", 30))).isNull();
    }

    @Test
    void negativeAge_returnsNull() throws Exception {
        assertThat(processor.process(dto("valid@example.com", "Charlie", -1))).isNull();
    }

    @Test
    void ageTooHigh_returnsNull() throws Exception {
        assertThat(processor.process(dto("valid@example.com", "Dave", 151))).isNull();
    }

    @Test
    void boundaryAge_returnsUser() throws Exception {
        assertThat(processor.process(dto("valid@example.com", "Eve", 0))).isNotNull();
        assertThat(processor.process(dto("valid@example.com", "Frank", 150))).isNotNull();
    }

    private UserImportDto dto(String email, String name, int age) {
        UserImportDto dto = new UserImportDto();
        dto.setEmail(email);
        dto.setName(name);
        dto.setAge(age);
        return dto;
    }

}