package io.github.js.infrastructure.persistence.article;

import io.github.js.domain.article.Article;
import io.github.js.domain.article.ArticleSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

/**
 * QueryDSL 기반 커스텀 리포지토리 인터페이스.
 *
 * - findByAnyTagName: IN + JOIN — Specification으로 표현하기 까다로운 쿼리를 QueryDSL로 작성
 * - searchArticles: BooleanExpression 동적 검색 — null 조건을 where() 가변인자로 자동 무시
 *
 * Specification 방식과의 비교:
 *   Specification   → cb.conjunction() 으로 null 안전 처리 필요
 *   BooleanExpression → null 반환 시 where()에서 자동 무시, 코드 더 직관적
 */
public interface ArticleRepositoryCustom {

    List<Article> findByAnyTagName(List<String> tagNames);

    Page<ArticleSummaryResponse> searchArticles(
            String authorName, String tag, String keyword, Instant createdAfter, Pageable pageable);
}
