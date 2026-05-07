package io.github.js.domain.article;

import io.github.js.domain.tag.Tag;
import io.github.js.domain.tag.TagRepository;
import io.github.js.domain.user.*;
import io.github.js.infrastructure.auditing.SecurityAuditorAware;
import io.github.js.infrastructure.config.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest: JPA 관련 빈만 로드 (Controller, Service 제외)
 * H2 인메모리 DB + @Transactional(rollback=true) 기본 적용
 * JpaConfig import 필요: @EnableJpaAuditing 포함
 */
@DataJpaTest
@Import({JpaConfig.class, SecurityAuditorAware.class})
@DisplayName("ArticleRepository 테스트")
class ArticleRepositoryTest {

    @Autowired ArticleRepository articleRepository;
    @Autowired UserRepository userRepository;
    @Autowired TagRepository tagRepository;
    @PersistenceContext EntityManager em;

    private User author;

    @BeforeEach
    void setUp() {
        SecurityAuditorAware.set("test-user");
        author = userRepository.save(
                User.of(new Email("test@test.com"), new UserName("tester"), new Password("pw")));
    }

    // -------------------------------------------------------------------------
    // N+1 시연 및 해결
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("N+1 문제: findAll() 후 author 접근 시 추가 쿼리 발생")
    void nPlusOneProblem() {
        articleRepository.save(Article.create(author, "title1", "desc1", "body1"));
        articleRepository.save(Article.create(author, "title2", "desc2", "body2"));

        em.flush();
        em.clear();

        Statistics stats = em.getEntityManagerFactory()
                .unwrap(org.hibernate.SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        List<Article> articles = articleRepository.findAll();
        // author 접근 → LAZY 로딩으로 각 Article마다 추가 쿼리 발생
        articles.forEach(a -> a.getAuthor().getUserName());

        long queryCount = stats.getPrepareStatementCount();
        // findAll 1 + author 조회 N = N+1 (여기서는 2+1=3 이지만 캐시/배치로 줄어들 수 있음)
        assertThat(queryCount).isGreaterThan(1);
    }

    @Test
    @DisplayName("N+1 해결 - JOIN FETCH: findByIdWithAuthor는 단일 쿼리로 author 포함")
    void joinFetchSolvesNPlusOne() {
        Article saved = articleRepository.save(Article.create(author, "title", "desc", "body"));
        em.flush();
        em.clear();

        Statistics stats = em.getEntityManagerFactory()
                .unwrap(org.hibernate.SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        Article article = articleRepository.findByIdWithAuthor(saved.getId()).orElseThrow();
        article.getAuthor().getUserName(); // 추가 쿼리 없어야 함

        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
        assertThat(article.getAuthor().getUserName().toString()).isEqualTo("tester");
    }

    @Test
    @DisplayName("N+1 해결 - @EntityGraph: author와 tags를 한 번에 로딩")
    void entityGraphSolvesNPlusOne() {
        Tag tag = tagRepository.save(Tag.of("java"));
        Article article = Article.create(author, "title", "desc", "body");
        article.addTag(tag);
        articleRepository.save(article);
        em.flush();
        em.clear();

        Statistics stats = em.getEntityManagerFactory()
                .unwrap(org.hibernate.SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        List<Article> articles = articleRepository.findAllWithAuthorAndTags();
        articles.forEach(a -> {
            a.getAuthor().getUserName();
            a.getTags().size();
        });

        // JOIN으로 author와 tags를 한 번에 가져오므로 쿼리 수 최소화
        assertThat(stats.getPrepareStatementCount()).isLessThanOrEqualTo(2);
    }

    // -------------------------------------------------------------------------
    // Page vs Slice
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Page<T>: totalElements와 totalPages를 포함한다")
    void pageIncludesTotalCount() {
        articleRepository.save(Article.create(author, "t1", "d1", "b1"));
        articleRepository.save(Article.create(author, "t2", "d2", "b2"));
        articleRepository.save(Article.create(author, "t3", "d3", "b3"));
        em.flush();
        em.clear();

        PageRequest pageable = PageRequest.of(0, 2, Sort.by("createdAt"));
        Page<ArticleSummaryResponse> page = articleRepository.findSummariesByPage(pageable);

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    @DisplayName("Slice<T>: COUNT 쿼리 없이 hasNext만 판단한다")
    void sliceHasNoCountQuery() {
        articleRepository.save(Article.create(author, "t1", "d1", "b1"));
        articleRepository.save(Article.create(author, "t2", "d2", "b2"));
        articleRepository.save(Article.create(author, "t3", "d3", "b3"));
        em.flush();
        em.clear();

        Statistics stats = em.getEntityManagerFactory()
                .unwrap(org.hibernate.SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        PageRequest pageable = PageRequest.of(0, 2);
        Slice<Article> slice = articleRepository.findByAuthorIdOrderByCreatedAtDesc(author.getId(), pageable);

        assertThat(slice.hasNext()).isTrue();
        assertThat(slice.getContent()).hasSize(2);
        // Slice는 COUNT 쿼리 없이 size+1 개만 조회해 hasNext 판단
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // Projection
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DTO Projection: ArticleSummaryResponse를 DB에서 직접 생성")
    void dtoProjctionReturnsCorrectData() {
        articleRepository.save(Article.create(author, "Hello JPA", "desc", "body"));
        em.flush();
        em.clear();

        Page<ArticleSummaryResponse> page = articleRepository.findSummariesByPage(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).title()).isEqualTo("Hello JPA");
        assertThat(page.getContent().get(0).authorName()).isEqualTo("tester");
    }

    @Test
    @DisplayName("Interface Projection: SpEL로 중첩 경로 접근")
    void interfaceProjectionReturnsCorrectData() {
        articleRepository.save(Article.create(author, "Interface Proj", "desc", "body"));
        em.flush();
        em.clear();

        List<ArticleSummaryProjection> projections =
                articleRepository.findProjectionsByAuthorId(author.getId());

        assertThat(projections).hasSize(1);
        assertThat(projections.get(0).getTitle()).isEqualTo("Interface Proj");
        assertThat(projections.get(0).getAuthorName()).isEqualTo("tester");
    }

    // -------------------------------------------------------------------------
    // Specification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Specification: 조건 조합으로 동적 필터링")
    void specificationFiltersCorrectly() {
        Tag javaTag = tagRepository.save(Tag.of("java"));
        Article a1 = Article.create(author, "Spring Boot", "desc", "body");
        a1.addTag(javaTag);
        articleRepository.save(a1);
        articleRepository.save(Article.create(author, "Python Guide", "desc", "body"));
        em.flush();
        em.clear();

        var spec = ArticleSpecification.hasAuthorName("tester")
                .and(ArticleSpecification.hasTag("java"));
        List<Article> result = articleRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Spring Boot");
    }

    // -------------------------------------------------------------------------
    // Custom Repository (QueryDSL)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("QueryDSL findByAnyTagName: 태그 OR 조건 + distinct로 중복 제거")
    void queryDslFindByAnyTagName() {
        Tag javaTag = tagRepository.save(Tag.of("java"));
        Tag springTag = tagRepository.save(Tag.of("spring"));
        Tag pythonTag = tagRepository.save(Tag.of("python"));

        Article a1 = Article.create(author, "Spring Boot", "d", "b");
        a1.addTag(javaTag);
        a1.addTag(springTag); // 두 태그 중 둘 다 매칭되어도 distinct로 1건
        articleRepository.save(a1);

        Article a2 = Article.create(author, "Python Guide", "d", "b");
        a2.addTag(pythonTag);
        articleRepository.save(a2);

        Article a3 = Article.create(author, "Go Tutorial", "d", "b"); // 태그 없음
        articleRepository.save(a3);

        em.flush();
        em.clear();

        List<Article> result = articleRepository.findByAnyTagName(List.of("java", "spring"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Spring Boot");
    }

    @Test
    @DisplayName("QueryDSL searchArticles: null 조건은 자동 무시되어 전체 결과 반환")
    void queryDslNullConditionsAreIgnored() {
        articleRepository.save(Article.create(author, "t1", "d", "b"));
        articleRepository.save(Article.create(author, "t2", "d", "b"));
        em.flush();
        em.clear();

        Page<ArticleSummaryResponse> result = articleRepository.searchArticles(
                null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("QueryDSL searchArticles: Specification 방식과 동일 결과 반환")
    void queryDslAndSpecificationReturnSameResult() {
        Tag javaTag = tagRepository.save(Tag.of("java"));
        Article a1 = Article.create(author, "Spring Boot", "d", "b");
        a1.addTag(javaTag);
        articleRepository.save(a1);
        articleRepository.save(Article.create(author, "Python Guide", "d", "b"));
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        Specification<Article> spec = ArticleSpecification.hasAuthorName("tester")
                .and(ArticleSpecification.titleContains("Spring"));
        List<String> specTitles = articleRepository.findAll(spec, pageable)
                .map(a -> a.getTitle())
                .getContent();

        Page<ArticleSummaryResponse> queryDslResult = articleRepository.searchArticles(
                "tester", null, "Spring", null, pageable);
        List<String> queryDslTitles = queryDslResult.getContent().stream()
                .map(ArticleSummaryResponse::title)
                .toList();

        assertThat(queryDslResult.getTotalElements()).isEqualTo(specTitles.size());
        assertThat(queryDslTitles).isEqualTo(specTitles);
    }

    // -------------------------------------------------------------------------
    // @Modifying + clearAutomatically
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("@Modifying clearAutomatically: 벌크 UPDATE 후 영속성 컨텍스트 자동 초기화")
    void modifyingClearAutomatically() {
        Article article = articleRepository.save(Article.create(author, "title", "desc", "body"));
        em.flush();

        // clearAutomatically=true → 벌크 UPDATE 후 영속성 컨텍스트 초기화
        articleRepository.incrementViewCount(article.getId());

        // 초기화 후 재조회하면 DB 반영값을 가져옴
        Article updated = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(updated.getViewCount()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // 소프트 삭제
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("소프트 삭제: @SQLDelete로 deleted=true, @SQLRestriction으로 조회 제외")
    void softDeleteHidesDeletedArticle() {
        Article article = articleRepository.save(Article.create(author, "title", "desc", "body"));
        em.flush();
        em.clear();

        articleRepository.delete(article);
        em.flush();
        em.clear();

        // @SQLRestriction("deleted = false") 적용으로 조회되지 않아야 함
        assertThat(articleRepository.findById(article.getId())).isEmpty();

        // native query로 deleted=true 확인
        Object deleted = em.createNativeQuery(
                "SELECT deleted FROM articles WHERE id = " + article.getId())
                .getSingleResult();
        assertThat(deleted).isEqualTo(true);
    }
}
