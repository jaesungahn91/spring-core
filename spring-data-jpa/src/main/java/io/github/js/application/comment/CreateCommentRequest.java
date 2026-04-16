package io.github.js.application.comment;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank String body
) {
}
