package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.User;
import io.github.js.batch.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Upsert 방식으로 User를 저장하는 공유 Writer.
 *
 * - 청크 내 email을 IN 절로 한 번에 조회
 * - 신규: saveAll() 일괄 insert
 * - 기존: update() 호출 후 JPA dirty checking으로 flush
 */
@Component
@RequiredArgsConstructor
public class UserItemWriter implements ItemWriter<User> {

    private final UserRepository userRepository;

    @Override
    public void write(Chunk<? extends User> chunk) {
        List<String> emails = chunk.getItems().stream()
                .map(User::getEmail)
                .toList();

        Map<String, User> existingMap = userRepository.findAllByEmailIn(emails).stream()
                .collect(Collectors.toMap(User::getEmail, u -> u));

        List<User> toInsert = new ArrayList<>();
        for (User incoming : chunk) {
            User existing = existingMap.get(incoming.getEmail());
            if (existing != null) {
                existing.update(incoming.getName(), incoming.getAge());
            } else {
                toInsert.add(incoming);
            }
        }

        if (!toInsert.isEmpty()) {
            userRepository.saveAll(toInsert);
        }
    }

}
