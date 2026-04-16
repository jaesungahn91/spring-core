package io.github.js.domain.article;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

/**
 * Specification 팩토리 시연.
 *
 * JpaSpecificationExecutor와 함께 사용해 동적 조건을 타입 안전하게 조합한다.
 * 각 Specification은 독립적이며 and()/or()로 조합 가능.
 *
 * 사용 예:
 *   articleRepository.findAll(
 *       hasAuthorName("john").and(hasTag("java")).and(createdAfter(instant)), pageable
 *   )
 */
public class ArticleSpecification {

    private ArticleSpecification() {
    }

    /**
     * null 조건은 cb.conjunction() (항상 참)을 반환해 체이닝 시 NPE 방지
     * → Specification.where() 없이 직접 .and() 체이닝 가능
     */
    public static Specification<Article> hasAuthorName(String username) {
        return (root, query, cb) ->
                username == null ? cb.conjunction() :
                cb.equal(root.get("author").get("profile").get("userName").get("nickname"), username);
    }

    public static Specification<Article> hasTag(String tagName) {
        return (root, query, cb) -> {
            if (tagName == null) return cb.conjunction();
            Join<Object, Object> tags = root.join("tags");
            return cb.equal(tags.get("name").get("value"), tagName.toLowerCase().trim());
        };
    }

    public static Specification<Article> createdAfter(Instant date) {
        return (root, query, cb) ->
                date == null ? cb.conjunction() :
                cb.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<Article> titleContains(String keyword) {
        return (root, query, cb) ->
                keyword == null ? cb.conjunction() :
                cb.like(cb.lower(root.get("contents").get("title").get("title")),
                        "%" + keyword.toLowerCase() + "%");
    }
}
