package io.github.js.application.article;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.js.application.GlobalExceptionHandler;
import io.github.js.domain.article.ArticleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleRestController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ArticleRestController 테스트")
class ArticleRestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ArticleService articleService;

    @Test
    @DisplayName("GET /articles: Pageable 파라미터 자동 바인딩")
    void getArticles_pageableBinding() throws Exception {
        ArticleSummaryResponse summary = new ArticleSummaryResponse(1L, "title", "author", Instant.now());
        given(articleService.getArticles(any()))
                .willReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/articles")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("title"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /articles: 존재하지 않는 게시글 조회 시 404 (ProblemDetail)")
    void getArticle_notFound_returns404() throws Exception {
        given(articleService.getArticle(99L))
                .willThrow(new EntityNotFoundException("Article not found: 99"));

        mockMvc.perform(get("/articles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    @DisplayName("PUT /articles/{id}: 낙관적 잠금 충돌 시 409 (ProblemDetail)")
    void updateArticle_optimisticLockConflict_returns409() throws Exception {
        willThrow(new OptimisticLockException("version mismatch"))
                .given(articleService).updateArticle(any(), any(), any(), any());

        CreateArticleRequest request = new CreateArticleRequest("title", "desc", "body", List.of());

        mockMvc.perform(put("/articles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Optimistic Lock Conflict"));
    }
}
