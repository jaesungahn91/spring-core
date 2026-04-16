package io.github.js.application.comment;

import io.github.js.domain.comment.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/articles/{articleId}/comments")
@RequiredArgsConstructor
public class CommentRestController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long articleId,
            @RequestParam Long authorId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = CommentResponse.from(
                commentService.addComment(articleId, authorId, request.body()));
        return ResponseEntity.created(URI.create("/articles/" + articleId + "/comments/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long articleId) {
        List<CommentResponse> responses = commentService.getComments(articleId)
                .stream().map(CommentResponse::from).toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
