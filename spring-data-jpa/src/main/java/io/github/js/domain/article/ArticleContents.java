package io.github.js.domain.article;

import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Embeddable
public class ArticleContents {

    @Embedded
    private ArticleTitle title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    private ArticleContents(ArticleTitle title, String description, String body) {
        this.title = title;
        this.description = description;
        this.body = body;
    }

    public static ArticleContents of(String title, String description, String body) {
        return new ArticleContents(ArticleTitle.of(title), description, body);
    }

    public void update(String title, String description, String body) {
        this.title = ArticleTitle.of(title);
        this.description = description;
        this.body = body;
    }
}
