package io.github.js.application.article;

import io.github.js.domain.article.Article;
import io.github.js.domain.tag.Tag;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record ArticleResponse(
        Long id,
        String title,
        String slug,
        String description,
        String body,
        String authorName,
        Set<String> tags,
        int viewCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static ArticleResponse from(Article article) {
        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getContents().getDescription(),
                article.getContents().getBody(),
                article.getAuthor().getUserName().toString(),
                article.getTags().stream().map(Tag::getName).map(Object::toString).collect(Collectors.toSet()),
                article.getViewCount(),
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }
}
