package io.github.js.infrastructure.persistence.article;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.js.domain.article.Article;
import io.github.js.domain.article.ArticleSummaryResponse;
import io.github.js.domain.article.QArticle;
import io.github.js.domain.tag.QTag;
import io.github.js.domain.user.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Article> findByAnyTagName(List<String> tagNames) {
        QArticle article = QArticle.article;
        QTag tag = QTag.tag;

        return queryFactory
                .selectFrom(article)
                .distinct()
                .innerJoin(article.author).fetchJoin()
                .innerJoin(article.tags, tag)
                .where(tag.name.value.in(tagNames))
                .fetch();
    }

    /**
     * QueryDSL 동적 검색 — Specification 방식의 searchArticles()와 동일 결과를 반환한다.
     *
     * count 쿼리 최적화:
     *   PageableExecutionUtils.getPage()는 마지막 페이지이거나 첫 페이지가 전체보다 작을 때만
     *   count 쿼리를 실행 → 불필요한 COUNT(*) 쿼리 생략.
     */
    // PathInits.DIRECT2는 QArticle 기준 depth 2까지만 초기화하므로
    // article.author.profile.userName(depth 3)은 null이 된다.
    // 명시적 alias QUser는 QUser 기준 depth 2로 초기화되어 profile.userName에 접근 가능하다.
    @Override
    public Page<ArticleSummaryResponse> searchArticles(
            String authorName, String tag, String keyword, Instant createdAfter, Pageable pageable) {

        QArticle article = QArticle.article;
        QUser author = new QUser("author");

        List<ArticleSummaryResponse> content = queryFactory
                .select(Projections.constructor(ArticleSummaryResponse.class,
                        article.id,
                        article.contents.title.title,
                        author.profile.userName.nickname,
                        article.createdAt))
                .from(article)
                .innerJoin(article.author, author)
                .where(
                        hasAuthorName(authorName, author),
                        hasTag(tag),
                        titleContains(keyword),
                        createdAfter(createdAfter)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(article.count())
                .from(article)
                .innerJoin(article.author, author)
                .where(
                        hasAuthorName(authorName, author),
                        hasTag(tag),
                        titleContains(keyword),
                        createdAfter(createdAfter)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression hasAuthorName(String authorName, QUser author) {
        return authorName == null ? null : author.profile.userName.nickname.eq(authorName);
    }

    // article.tags.any() → EXISTS 서브쿼리 생성 — DISTINCT 없이 단일 태그 매칭
    private BooleanExpression hasTag(String tagName) {
        return tagName == null ? null :
                QArticle.article.tags.any().name.value.eq(tagName.toLowerCase().trim());
    }

    private BooleanExpression titleContains(String keyword) {
        return keyword == null ? null :
                QArticle.article.contents.title.title.containsIgnoreCase(keyword);
    }

    private BooleanExpression createdAfter(Instant date) {
        return date == null ? null :
                QArticle.article.createdAt.goe(date);
    }
}
