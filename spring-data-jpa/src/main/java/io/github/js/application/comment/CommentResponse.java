package io.github.js.application.comment;

import io.github.js.domain.comment.Comment;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String body,
        String authorName,
        Instant createdAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getBody().getValue(),
                comment.getAuthor().getUserName().toString(),
                comment.getCreatedAt()
        );
    }
}
