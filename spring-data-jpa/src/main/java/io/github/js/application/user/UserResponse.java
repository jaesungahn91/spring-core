package io.github.js.application.user;

import io.github.js.domain.user.User;

public record UserResponse(
        Long id,
        String email,
        String username,
        String bio,
        String image
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail().toString(),
                user.getUserName().toString(),
                user.getProfile().getBio(),
                null // Image VO는 address 미노출 — 필요 시 Image에 getter 추가
        );
    }
}
