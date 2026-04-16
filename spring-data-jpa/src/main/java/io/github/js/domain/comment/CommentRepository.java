package io.github.js.domain.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * N+1 발생 시나리오: 각 comment.author를 개별 쿼리로 로딩
     */
    List<Comment> findByArticleId(Long articleId);

    /**
     * JOIN FETCH로 N+1 해결: author를 단일 쿼리로 함께 조회
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.article.id = :articleId ORDER BY c.createdAt DESC")
    List<Comment> findByArticleIdWithAuthor(@Param("articleId") Long articleId);

    /**
     * 벌크 소프트 삭제: 게시글 삭제 시 연관 댓글 일괄 처리
     * clearAutomatically: 벌크 연산 후 영속성 컨텍스트 stale 방지
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c SET c.deleted = true WHERE c.article.id = :articleId")
    int softDeleteByArticleId(@Param("articleId") Long articleId);
}
