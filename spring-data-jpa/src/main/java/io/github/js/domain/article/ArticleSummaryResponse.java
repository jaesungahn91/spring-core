package io.github.js.domain.article;

import java.time.Instant;

/**
 * DTO Projection용 record.
 *
 * JPQL 생성자 표현식에서 사용:
 *   SELECT new io.github.js.domain.article.ArticleSummaryResponse(a.id, ...) FROM Article a
 *
 * record는 Java 17+에서 불변 DTO의 표준 방식.
 * equals/hashCode/toString/생성자가 자동 생성된다.
 */
public record ArticleSummaryResponse(
        Long id,
        String title,
        String authorName,
        Instant createdAt
) {
}
