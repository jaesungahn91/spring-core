package io.github.js.domain.comment;

import io.github.js.domain.article.Article;
import io.github.js.domain.article.ArticleRepository;
import io.github.js.domain.user.Email;
import io.github.js.domain.user.Password;
import io.github.js.domain.user.User;
import io.github.js.domain.user.UserName;
import io.github.js.domain.user.UserRepository;
import io.github.js.infrastructure.auditing.SecurityAuditorAware;
import io.github.js.infrastructure.repository.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaConfig.class, SecurityAuditorAware.class})
@DisplayName("CommentRepository 테스트")
class CommentRepositoryTest {

    @Autowired CommentRepository commentRepository;
    @Autowired ArticleRepository articleRepository;
    @Autowired UserRepository userRepository;
    @PersistenceContext EntityManager em;

    private User author;
    private Article article;

    @BeforeEach
    void setUp() {
        SecurityAuditorAware.set("test-user");
        author = userRepository.save(
                User.of(new Email("test@test.com"), new UserName("tester"), new Password("pw")));
        article = articleRepository.save(Article.create(author, "title", "desc", "body"));
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("JOIN FETCH: findByArticleIdWithAuthor는 author를 단일 쿼리로 조회")
    void joinFetchLoadsAuthorInSingleQuery() {
        commentRepository.save(Comment.create(article, author, CommentBody.of("comment1")));
        commentRepository.save(Comment.create(article, author, CommentBody.of("comment2")));
        em.flush();
        em.clear();

        List<Comment> comments = commentRepository.findByArticleIdWithAuthor(article.getId());

        assertThat(comments).hasSize(2);
        // author가 이미 로딩되어 있어야 함 (추가 쿼리 없이 접근 가능)
        comments.forEach(c -> assertThat(c.getAuthor().getUserName().toString()).isEqualTo("tester"));
    }

    @Test
    @DisplayName("@Modifying 벌크 소프트 삭제: 게시글의 댓글 전체 일괄 처리")
    void bulkSoftDeleteByArticleId() {
        commentRepository.save(Comment.create(article, author, CommentBody.of("c1")));
        commentRepository.save(Comment.create(article, author, CommentBody.of("c2")));
        em.flush();
        em.clear();

        int deleted = commentRepository.softDeleteByArticleId(article.getId());

        assertThat(deleted).isEqualTo(2);
        // @SQLRestriction으로 조회에서 제외되어야 함
        assertThat(commentRepository.findByArticleIdWithAuthor(article.getId())).isEmpty();
    }

    @Test
    @DisplayName("소프트 삭제: Comment 삭제 시 DB에 deleted=true 기록")
    void softDeleteSetsDeletedFlag() {
        Comment comment = commentRepository.save(
                Comment.create(article, author, CommentBody.of("to be deleted")));
        em.flush();
        em.clear();

        commentRepository.delete(comment);
        em.flush();
        em.clear();

        assertThat(commentRepository.findById(comment.getId())).isEmpty();

        Object deleted = em.createNativeQuery(
                "SELECT deleted FROM comments WHERE id = " + comment.getId())
                .getSingleResult();
        assertThat(deleted).isEqualTo(true);
    }
}
