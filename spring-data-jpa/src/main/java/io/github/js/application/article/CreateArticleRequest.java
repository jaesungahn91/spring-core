package io.github.js.application.article;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateArticleRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String body,
        List<String> tags
) {
    public CreateArticleRequest {
        if (tags == null) tags = List.of();
    }
}
