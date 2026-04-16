package io.github.js.domain.article;

import io.github.js.domain.BaseEntity;
import io.github.js.domain.comment.Comment;
import io.github.js.domain.tag.Tag;
import io.github.js.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

/**
 * BaseEntity 상속: @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy 자동 관리
 *
 * fetch=LAZY 명시:
 * - @ManyToOne 기본값은 EAGER → N+1 문제의 주요 원인
 * - LAZY로 설정해 실제 접근 시점에 쿼리 실행 (JOIN FETCH / @EntityGraph로 제어)
 *
 * @ManyToMany:
 * - cascade={PERSIST,MERGE}만 적용: Tag는 독립 생명주기이므로 REMOVE 제외
 * - 조인 테이블 명시: article_tags
 *
 * @OneToMany(orphanRemoval=true):
 * - Comment는 Article 없이 존재 불가 → orphanRemoval로 자동 삭제
 */
@Getter
@Entity
@Table(name = "articles")
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE articles SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Embedded
    private ArticleContents contents;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private int viewCount = 0;

    /**
     * @ManyToMany: Article이 연관관계의 주인 (JoinTable 보유)
     * cascade={PERSIST,MERGE}: 새 Tag를 Article과 함께 저장/병합 가능, 삭제는 전파 안 함
     */
    @ManyToMany(cascade = {PERSIST, MERGE})
    @JoinTable(
            name = "article_tags",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    /**
     * orphanRemoval=true: Article에서 제거된 Comment는 DB에서도 삭제
     * Comment는 소프트 삭제(@SQLDelete)이므로 실제로는 UPDATE deleted=true 실행
     */
    @OneToMany(mappedBy = "article", cascade = ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    private Article(User author, ArticleContents contents) {
        this.author = author;
        this.contents = contents;
        author.addArticle(this);
    }

    public static Article create(User author, String title, String description, String body) {
        return new Article(author, ArticleContents.of(title, description, body));
    }

    public void update(String title, String description, String body) {
        this.contents.update(title, description, body);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public String getSlug() {
        return contents.getTitle().getSlug();
    }

    public String getTitle() {
        return contents.getTitle().getTitle();
    }
}
