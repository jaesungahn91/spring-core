package io.github.js.domain.article;

import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;

/**
 * Interface-based Projection 시연.
 *
 * Spring Data JPA가 프록시를 생성해 인터페이스 메서드를 구현한다.
 * @Value SpEL로 엔티티 중첩 경로에 접근 가능 (별도 JOIN 없이 처리).
 *
 * 장점: 별도 클래스 없이 필요한 필드만 조회 → 쿼리 최적화
 * 단점: SpEL 표현식이 복잡해지면 가독성 저하
 */
public interface ArticleSummaryProjection {

    Long getId();

    // SpEL로 중첩 Embedded 경로 접근: Article.contents.title.title
    @Value("#{target.contents.title.title}")
    String getTitle();

    // SpEL로 연관 엔티티 경로 접근 (LAZY 로딩 발생 주의 → JOIN FETCH 필요)
    @Value("#{target.author.profile.userName.nickname}")
    String getAuthorName();

    Instant getCreatedAt();
}
