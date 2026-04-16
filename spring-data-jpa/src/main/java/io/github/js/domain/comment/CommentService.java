package io.github.js.domain.comment;

import io.github.js.domain.article.Article;
import io.github.js.domain.article.ArticleRepository;
import io.github.js.domain.user.User;
import io.github.js.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    public Comment addComment(Long articleId, Long authorId, String body) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("Article not found: " + articleId));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + authorId));

        Comment comment = Comment.create(article, author, CommentBody.of(body));
        article.addComment(comment);
        return commentRepository.save(comment);
    }

    /**
     * JOIN FETCH로 N+1 방지: 각 comment의 author를 단일 쿼리로 조회
     */
    @Transactional(readOnly = true)
    public List<Comment> getComments(Long articleId) {
        return commentRepository.findByArticleIdWithAuthor(articleId);
    }

    /**
     * 소프트 삭제: @SQLDelete에 의해 UPDATE deleted=true 실행
     */
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + commentId));
        commentRepository.delete(comment);
    }
}
