package io.github.js.application.article;

import java.time.Instant;

/**
 * 동적 검색 조건 record.
 * null 필드는 ArticleSpecification에서 cb.conjunction()으로 처리되어 조건에서 제외된다.
 */
public record ArticleSearchCondition(
        String authorName,
        String tag,
        String keyword,
        Instant createdAfter
) {
}
