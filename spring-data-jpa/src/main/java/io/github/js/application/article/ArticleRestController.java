package io.github.js.application.article;

import io.github.js.domain.article.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleRestController {

    private final ArticleService articleService;

    @PostMapping
    public ResponseEntity<ArticleResponse> createArticle(
            @RequestParam Long authorId,
            @Valid @RequestBody CreateArticleRequest request) {
        ArticleResponse response = ArticleResponse.from(
                articleService.createArticle(authorId, request.title(), request.description(),
                        request.body(), request.tags()));
        return ResponseEntity.created(URI.create("/articles/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(ArticleResponse.from(articleService.getArticle(id)));
    }

    /**
     * 페이지네이션: ?page=0&size=10&sort=createdAt,desc
     * Pageable은 Spring MVC가 쿼리 파라미터를 자동으로 바인딩한다.
     * Page<T> 응답에는 totalElements, totalPages, hasNext 등 메타 정보가 포함된다.
     */
    @GetMapping
    public ResponseEntity<Page<ArticleSummaryResponse>> getArticles(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(articleService.getArticles(pageable));
    }

    /**
     * Slice 기반 피드: ?page=0&size=5
     * totalElements 없이 hasNext만 반환 → 무한 스크롤 UI에 적합
     */
    @GetMapping("/feed")
    public ResponseEntity<Slice<ArticleResponse>> getFeed(
            @RequestParam Long authorId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                articleService.getFeed(authorId, pageable).map(ArticleResponse::from));
    }

    /**
     * Specification 기반 동적 검색: 조건별 필터링
     * null 파라미터는 자동 무시
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ArticleSummaryResponse>> searchArticles(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Instant createdAfter,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                articleService.searchArticles(author, tag, keyword, createdAfter, pageable));
    }

    /**
     * 낙관적 잠금: @Version 불일치 시 OptimisticLockException → GlobalExceptionHandler에서 409 반환
     */
    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody CreateArticleRequest request) {
        return ResponseEntity.ok(ArticleResponse.from(
                articleService.updateArticle(id, request.title(), request.description(), request.body())));
    }

    /**
     * 소프트 삭제: @SQLDelete에 의해 UPDATE deleted=true 실행
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * @Modifying 벌크 UPDATE 시연: 조회수 증가
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long id) {
        articleService.incrementViewCount(id);
        return ResponseEntity.noContent().build();
    }
}
