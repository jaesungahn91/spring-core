package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.User;
import org.springframework.batch.item.ItemProcessor;

import java.util.regex.Pattern;

public class UserItemProcessor implements ItemProcessor<UserCsvDto, User> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    @Override
    public User process(UserCsvDto dto) {
        if (!EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            return null;
        }
        if (dto.getAge() < 0 || dto.getAge() > 150) {
            return null;
        }
        return new User(dto.getEmail(), dto.getName(), dto.getAge());
    }

}