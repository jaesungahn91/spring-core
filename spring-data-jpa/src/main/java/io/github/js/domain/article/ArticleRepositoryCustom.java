package io.github.js.domain.article;

import java.util.List;

/**
 * 커스텀 리포지토리 인터페이스.
 *
 * ArticleRepository가 이 인터페이스를 extends하면
 * Spring Data JPA가 ArticleRepositoryImpl을 자동으로 찾아 위임한다.
 * (네이밍 규칙: {리포지토리명}Impl)
 *
 * 사용 목적: 복잡한 동적 쿼리나 EntityManager를 직접 다뤄야 하는 경우 캡슐화
 */
public interface ArticleRepositoryCustom {

    /**
     * 여러 태그 중 하나라도 일치하는 게시글 조회 (OR 조건).
     * Specification으로 표현하기 까다로운 IN + JOIN 쿼리를 QueryDSL로 타입 안전하게 작성.
     */
    List<Article> findByAnyTagName(List<String> tagNames);
}
