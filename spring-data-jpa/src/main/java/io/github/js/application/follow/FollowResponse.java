package io.github.js.application.follow;

import io.github.js.domain.follow.Follow;

import java.time.Instant;

public record FollowResponse(
        Long followerId,
        String followerName,
        Long followeeId,
        String followeeName,
        Instant followedAt
) {
    public static FollowResponse from(Follow follow) {
        return new FollowResponse(
                follow.getFollower().getId(),
                follow.getFollower().getUserName().toString(),
                follow.getFollowee().getId(),
                follow.getFollowee().getUserName().toString(),
                follow.getCreatedAt()
        );
    }
}
