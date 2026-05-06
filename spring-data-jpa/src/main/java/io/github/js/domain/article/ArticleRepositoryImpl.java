package io.github.js.domain.article;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.js.domain.tag.QTag;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 커스텀 리포지토리 구현체.
 *
 * 네이밍 규칙: {리포지토리 인터페이스명}Impl
 * Spring Data JPA가 ArticleRepository 빈 생성 시 자동으로 이 구현체를 포함한다.
 *
 * QueryDSL 기반 구현:
 * - JPAQueryFactory로 쿼리를 조립 → Q클래스가 컴파일타임에 필드/경로 타입을 검증
 * - JPQL 문자열 대비 오타/필드명 변경 시 컴파일 에러로 조기 감지
 * - 동적 조건, Join, Projection 조합이 문자열 결합보다 가독성/안전성 우위
 */
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
}
