package io.github.js.domain.comment;

import io.github.js.domain.BaseEntity;
import io.github.js.domain.article.Article;
import io.github.js.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * 소프트 삭제 시연:
 * - @SQLDelete: DELETE 대신 UPDATE deleted=true 실행
 * - @SQLRestriction: 모든 조회에 deleted=false 조건 자동 적용
 */
@Getter
@Entity
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE comments SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Embedded
    private CommentBody body;

    @Column(nullable = false)
    private boolean deleted = false;

    private Comment(Article article, User author, CommentBody body) {
        this.article = article;
        this.author = author;
        this.body = body;
    }

    public static Comment create(Article article, User author, CommentBody body) {
        return new Comment(article, author, body);
    }
}
