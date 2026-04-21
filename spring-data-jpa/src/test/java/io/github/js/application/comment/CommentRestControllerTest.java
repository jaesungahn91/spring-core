package io.github.js.application.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.js.application.GlobalExceptionHandler;
import io.github.js.domain.comment.Comment;
import io.github.js.domain.comment.CommentBody;
import io.github.js.domain.comment.CommentService;
import io.github.js.domain.user.User;
import io.github.js.domain.user.UserName;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentRestController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CommentRestController 테스트")
class CommentRestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean CommentService commentService;

    @Test
    @DisplayName("POST /articles/{articleId}/comments: 댓글 생성 성공 시 201 반환")
    void addComment_success_returns201() throws Exception {
        CommentResponse response = new CommentResponse(1L, "댓글 내용", "author", Instant.now());
        Comment stub = stubComment(response);
        given(commentService.addComment(anyLong(), anyLong(), any())).willReturn(stub);

        mockMvc.perform(post("/articles/1/comments")
                        .param("authorId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCommentRequest("댓글 내용"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("댓글 내용"))
                .andExpect(jsonPath("$.authorName").value("author"));
    }

    @Test
    @DisplayName("POST /articles/{articleId}/comments: body가 빈 문자열이면 422 반환")
    void addComment_blankBody_returns422() throws Exception {
        mockMvc.perform(post("/articles/1/comments")
                        .param("authorId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCommentRequest(""))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /articles/{articleId}/comments: 존재하지 않는 article이면 404 반환")
    void addComment_articleNotFound_returns404() throws Exception {
        given(commentService.addComment(anyLong(), anyLong(), any()))
                .willThrow(new EntityNotFoundException("Article not found: 99"));

        mockMvc.perform(post("/articles/99/comments")
                        .param("authorId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCommentRequest("내용"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /articles/{articleId}/comments: 댓글 목록 조회 성공")
    void getComments_success_returnsList() throws Exception {
        CommentResponse c1 = new CommentResponse(1L, "첫 번째 댓글", "userA", Instant.now());
        CommentResponse c2 = new CommentResponse(2L, "두 번째 댓글", "userB", Instant.now());
        Comment stub1 = stubComment(c1);
        Comment stub2 = stubComment(c2);
        given(commentService.getComments(1L)).willReturn(List.of(stub1, stub2));

        mockMvc.perform(get("/articles/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].body").value("첫 번째 댓글"));
    }

    @Test
    @DisplayName("DELETE /articles/{articleId}/comments/{commentId}: 삭제 성공 시 204 반환")
    void deleteComment_success_returns204() throws Exception {
        mockMvc.perform(delete("/articles/1/comments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /articles/{articleId}/comments/{commentId}: 존재하지 않는 댓글이면 404 반환")
    void deleteComment_notFound_returns404() throws Exception {
        willThrow(new EntityNotFoundException("Comment not found: 99"))
                .given(commentService).deleteComment(99L);

        mockMvc.perform(delete("/articles/1/comments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private Comment stubComment(CommentResponse response) {
        Comment comment = mock(Comment.class);
        CommentBody body = mock(CommentBody.class);
        User author = mock(User.class);
        UserName userName = new UserName(response.authorName());

        when(comment.getId()).thenReturn(response.id());
        when(comment.getBody()).thenReturn(body);
        when(body.getValue()).thenReturn(response.body());
        when(comment.getAuthor()).thenReturn(author);
        when(author.getUserName()).thenReturn(userName);
        when(comment.getCreatedAt()).thenReturn(response.createdAt());

        return comment;
    }
}
