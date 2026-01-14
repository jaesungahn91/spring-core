package io.github.js.domain.article;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import static lombok.AccessLevel.PROTECTED;

@EqualsAndHashCode
@Getter
@NoArgsConstructor(access = PROTECTED)
@Embeddable
public class ArticleTitle {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String slug;

    public static ArticleTitle of(String title) {
        return new ArticleTitle(title, slugFromTitle(title));
    }

    private ArticleTitle(String title, String slug) {
        this.title = title;
        this.slug = slug;
    }

    private static String slugFromTitle(String title) {
        return title.toLowerCase()
                .replaceAll("\\$,'\"|\\s|\\.|\\?", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("(^-)|(-$)", "");
    }

}
