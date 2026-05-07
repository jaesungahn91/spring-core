package io.github.js.domain.article;

import io.github.js.domain.tag.Tag;
import io.github.js.domain.tag.TagName;
import io.github.js.domain.tag.TagRepository;
import io.github.js.domain.user.User;
import io.github.js.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    @Transactional
    public Article createArticle(Long authorId, String title, String description, String body, List<String> tagNames) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + authorId));

        Article article = Article.create(author, title, description, body);

        // 태그 재사용 or 신규 생성: 기존 태그 일괄 조회 후 없는 이름만 신규 생성
        // TagName이 @EqualsAndHashCode + 생성자에서 정규화하므로 Set<TagName>으로 비교
        List<TagName> requestedNames = tagNames.stream().map(TagName::of).toList();
        List<Tag> existingTags = tagRepository.findByNameIn(requestedNames);
        Set<TagName> existingNameSet = existingTags.stream().map(Tag::getName).collect(Collectors.toSet());

        existingTags.forEach(article::addTag);
        requestedNames.stream()
                .filter(name -> !existingNameSet.contains(name))
                .map(name -> Tag.of(name.getValue()))
                .forEach(article::addTag);

        return articleRepository.save(article);
    }

    /**
     * JOIN FETCH로 N+1 방지: author를 단일 쿼리로 함께 조회
     */
    @Transactional(readOnly = true)
    public Article getArticle(Long id) {
        return articleRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found: " + id));
    }

    /**
     * Page<T>: 총 개수 포함 — 게시판형 목록 API
     */
    @Transactional(readOnly = true)
    public Page<ArticleSummaryResponse> getArticles(Pageable pageable) {
        return articleRepository.findSummariesByPage(pageable);
    }

    /**
     * Slice<T>: 다음 페이지 존재 여부만 확인 — 무한 스크롤 API
     */
    @Transactional(readOnly = true)
    public Slice<Article> getFeed(Long authorId, Pageable pageable) {
        return articleRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
    }

    /**
     * [Specification] 동적 검색: null 조건은 cb.conjunction()으로 무시
     */
    @Transactional(readOnly = true)
    public Page<ArticleSummaryResponse> searchArticles(
            String authorName, String tag, String keyword, Instant createdAfter, Pageable pageable) {

        Specification<Article> spec = ArticleSpecification.hasAuthorName(authorName)
                .and(ArticleSpecification.hasTag(tag))
                .and(ArticleSpecification.titleContains(keyword))
                .and(ArticleSpecification.createdAfter(createdAfter));

        return articleRepository.findAll(spec, pageable)
                .map(a -> new ArticleSummaryResponse(
                        a.getId(),
                        a.getTitle(),
                        a.getAuthor().getProfile().getUserName().getNickname(),
                        a.getCreatedAt()));
    }

    /**
     * [QueryDSL] 동적 검색: null 조건은 BooleanExpression null 반환으로 where()에서 자동 무시
     * searchArticles()와 동일 결과를 반환하며, 두 방식의 비교 학습을 위해 병렬 제공한다.
     */
    @Transactional(readOnly = true)
    public Page<ArticleSummaryResponse> searchArticlesByQueryDsl(
            String authorName, String tag, String keyword, Instant createdAfter, Pageable pageable) {

        return articleRepository.searchArticles(authorName, tag, keyword, createdAfter, pageable);
    }

    /**
     * 낙관적 잠금 시연:
     * @Version 필드가 DB와 불일치하면 OptimisticLockException 발생
     * → GlobalExceptionHandler에서 409 Conflict로 변환
     */
    @Transactional
    public Article updateArticle(Long id, String title, String description, String body) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found: " + id));
        article.update(title, description, body);
        return article; // dirty checking으로 자동 UPDATE
    }

    /**
     * 소프트 삭제:
     * @SQLDelete로 DELETE → UPDATE deleted=true 변환
     * 연관 댓글도 @Modifying 벌크로 일괄 소프트 삭제
     */
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found: " + id));
        articleRepository.delete(article);
    }

    /**
     * @Modifying 벌크 UPDATE: 영속성 컨텍스트 우회 — 대량 처리에 적합
     */
    @Transactional
    public void incrementViewCount(Long id) {
        articleRepository.incrementViewCount(id);
    }
}
