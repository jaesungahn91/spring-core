package io.github.js.batch.job.user;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserApiItemReaderTest {

    @Test
    void read_iteratesAllSimulatedItems() throws Exception {
        UserApiItemReader reader = new SimulatedUserApiItemReader();

        List<UserImportDto> results = new ArrayList<>();
        UserImportDto item;
        while ((item = reader.read()) != null) {
            results.add(item);
        }

        assertThat(results).hasSize(25);
    }

    @Test
    void read_returnsNull_whenExhausted() throws Exception {
        UserApiItemReader reader = new SimulatedUserApiItemReader();
        while (reader.read() != null) {}

        assertThat(reader.read()).isNull();
    }

    @Test
    void read_firstItem_isCorrect() throws Exception {
        UserApiItemReader reader = new SimulatedUserApiItemReader();
        UserImportDto first = reader.read();

        assertThat(first).isNotNull();
        assertThat(first.getEmail()).isEqualTo("user0@example.com");
        assertThat(first.getName()).isEqualTo("User 0");
    }

}