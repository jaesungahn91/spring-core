package io.github.js.domain.article;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 커스텀 리포지토리 구현체.
 *
 * 네이밍 규칙: {리포지토리 인터페이스명}Impl
 * Spring Data JPA가 ArticleRepository 빈 생성 시 자동으로 이 구현체를 포함한다.
 *
 * EntityManager를 직접 주입받아 JPQL을 작성한다.
 */
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<Article> findByAnyTagName(List<String> tagNames) {
        return em.createQuery(
                        "SELECT DISTINCT a FROM Article a " +
                        "JOIN FETCH a.author " +
                        "JOIN a.tags t " +
                        "WHERE t.name.value IN :tagNames",
                        Article.class)
                .setParameter("tagNames", tagNames)
                .getResultList();
    }
}
