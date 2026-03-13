package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.User;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserItemProcessor implements ItemProcessor<UserImportDto, User> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    @Override
    public User process(UserImportDto dto) {
        if (dto.getEmail() == null || !EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            return null;
        }
        if (dto.getAge() < 0 || dto.getAge() > 150) {
            return null;
        }
        return new User(dto.getEmail(), dto.getName(), dto.getAge());
    }

}