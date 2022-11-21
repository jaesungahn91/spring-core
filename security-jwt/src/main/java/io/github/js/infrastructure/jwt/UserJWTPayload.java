package io.github.js.infrastructure.jwt;

import io.github.js.domain.jwt.JWTPayload;
import io.github.js.domain.user.User;
import lombok.Getter;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.time.Instant.now;

@Getter
public class UserJWTPayload implements JWTPayload {

    private long sub;
    private String name;
    private long iat;

    public static UserJWTPayload of(User user, long epochSecondExpired) {
        return new UserJWTPayload(user.getId(), valueOf(user.getEmail()), epochSecondExpired);
    }

    private UserJWTPayload(long sub, String name, long iat) {
        this.sub = sub;
        this.name = name;
        this.iat = iat;
    }

    protected UserJWTPayload() {
    }

    @Override
    public long getUserId() {
        return sub;
    }

    @Override
    public boolean isExpired() {
        return iat < now().getEpochSecond();
    }

    @Override
    public String toString() {
        return format("{\"sub\":%d,\"name\":\"%s\",\"iat\":%d}", sub, name, iat);
    }

}
