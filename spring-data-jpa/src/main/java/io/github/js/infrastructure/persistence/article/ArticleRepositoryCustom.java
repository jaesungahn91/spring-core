package io.github.js.infrastructure.persistence.article;

import io.github.js.domain.article.Article;
import io.github.js.domain.article.ArticleSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface ArticleRepositoryCustom {

    List<Article> findByAnyTagName(List<String> tagNames);

    Page<ArticleSummaryResponse> searchArticles(
            String authorName, String tag, String keyword, Instant createdAfter, Pageable pageable);

    // EXISTS 서브쿼리: 댓글이 하나라도 있는 게시글 조회
    List<Article> findArticlesHavingComment();

    // 동적 정렬: 런타임에 결정되는 정렬 기준을 OrderSpecifier로 표현
    List<Article> findWithDynamicSort(String sortField, boolean asc, Pageable pageable);

    // exists 최적화: COUNT(*) 대신 SELECT 1 ... LIMIT 1 — 첫 행 발견 즉시 중단
    boolean existsArticleByAuthorId(Long authorId);
}
