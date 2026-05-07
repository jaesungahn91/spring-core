package io.github.js.domain.article;

import io.github.js.infrastructure.persistence.article.ArticleRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends
        JpaRepository<Article, Long>,
        JpaSpecificationExecutor<Article>,   // Specification 동적 쿼리
        ArticleRepositoryCustom {            // QueryDSL 커스텀 리포지토리 (infra)

    // -----------------------------------------------------------------------
    // N+1 문제 시연 및 해결
    // -----------------------------------------------------------------------

    /**
     * [N+1 발생] author를 LAZY 로딩 → 목록 조회 후 각 author 접근 시 N개 추가 쿼리
     * ArticleRepositoryTest에서 이 메서드로 N+1을 확인하고 아래 메서드로 해결을 검증한다.
     */
    List<Article> findAll();

    /**
     * [N+1 해결 - JOIN FETCH] author를 단일 쿼리로 함께 조회
     * DISTINCT: 컬렉션 JOIN 시 중복 행 제거
     */
    @Query("SELECT a FROM Article a JOIN FETCH a.author WHERE a.id = :id")
    Optional<Article> findByIdWithAuthor(@Param("id") Long id);

    /**
     * [N+1 해결 - @EntityGraph] attributePaths로 EAGER 로딩할 연관관계 지정
     * JOIN FETCH와 동일한 효과. 어노테이션 기반이라 재사용성이 높다.
     */
    @EntityGraph(attributePaths = {"author", "tags"})
    @Query("SELECT a FROM Article a")
    List<Article> findAllWithAuthorAndTags();

    // -----------------------------------------------------------------------
    // 다양한 쿼리 방식
    // -----------------------------------------------------------------------

    /**
     * 메서드 이름 파생 쿼리: Spring Data JPA가 메서드명을 파싱해 JPQL 자동 생성
     */
    List<Article> findByAuthorId(Long authorId);

    /**
     * native query: DB 벤더 종속적이지만 복잡한 SQL이나 DB 전용 함수 사용 시 활용
     * @SQLRestriction은 native query에 자동 적용되지 않으므로 조건 명시 필요
     */
    @Query(value = "SELECT * FROM articles WHERE slug = :slug AND deleted = false",
           nativeQuery = true)
    Optional<Article> findBySlug(@Param("slug") String slug);

    /**
     * @Modifying 벌크 UPDATE: 영속성 컨텍스트를 거치지 않고 DB 직접 업데이트
     * clearAutomatically=true: 벌크 연산 후 영속성 컨텍스트를 초기화해 stale 방지
     * flushAutomatically=true: 벌크 연산 전 변경 사항을 먼저 flush
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    int incrementViewCount(@Param("id") Long id);

    /**
     * @Modifying 벌크 소프트 삭제: 특정 작성자의 모든 게시글을 한 번에 삭제 처리
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Article a SET a.deleted = true WHERE a.author.id = :authorId")
    int softDeleteByAuthorId(@Param("authorId") Long authorId);

    // -----------------------------------------------------------------------
    // 페이지네이션
    // -----------------------------------------------------------------------

    /**
     * Page<T>: COUNT 쿼리를 추가로 실행해 전체 페이지 수 / 총 요소 수 포함
     * 게시판형 UI (페이지 번호 표시)에 적합
     */
    @Query("SELECT new io.github.js.domain.article.ArticleSummaryResponse(" +
           "a.id, a.contents.title.title, a.author.profile.userName.nickname, a.createdAt) " +
           "FROM Article a JOIN a.author")
    Page<ArticleSummaryResponse> findSummariesByPage(Pageable pageable);

    /**
     * [N+1 해결 - Specification + @EntityGraph]
     * JpaSpecificationExecutor.findAll(Specification, Pageable)을 오버라이드하여
     * author를 EAGER 로딩 — searchArticles()에서 발생하던 N+1을 제거한다.
     */
    @EntityGraph(attributePaths = {"author"})
    @Override
    Page<Article> findAll(Specification<Article> spec, Pageable pageable);

    /**
     * Slice<T>: COUNT 쿼리 없이 다음 페이지 존재 여부(hasNext)만 확인
     * 무한 스크롤 UI에 적합 (COUNT 쿼리 비용 절감)
     */
    Slice<Article> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    // -----------------------------------------------------------------------
    // 프로젝션
    // -----------------------------------------------------------------------

    /**
     * Interface-based Projection: 필요한 필드만 담은 프록시 객체 반환
     * @Value SpEL로 중첩 경로 접근 (ArticleSummaryProjection 참고)
     */
    @EntityGraph(attributePaths = {"author"})
    List<ArticleSummaryProjection> findProjectionsByAuthorId(Long authorId);
}
