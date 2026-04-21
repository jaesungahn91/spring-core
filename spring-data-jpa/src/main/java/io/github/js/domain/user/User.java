package io.github.js.domain.user;

import io.github.js.domain.BaseEntity;
import io.github.js.domain.article.Article;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.springframework.util.StringUtils.hasText;

/**
 * @Version: 낙관적 잠금 — 동시 수정 시 나중에 flush하는 쪽에서 OptimisticLockException 발생
 * @SQLDelete: DELETE 쿼리 대신 UPDATE deleted=true 실행 (소프트 삭제)
 * @SQLRestriction: 모든 조회 쿼리에 deleted=false 조건 자동 적용
 */
@Getter
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Embedded
    private Email email;

    @Embedded
    private Profile profile;

    @Embedded
    private Password password;

    @Column(nullable = false)
    private boolean deleted = false;

    /**
     * 양방향 @OneToMany — User가 연관관계의 비주인(mappedBy)
     * cascade=ALL: User 저장/삭제 시 Article도 함께 처리
     * 연관관계 편의 메서드로 양방향 동기화 유지
     */
    @OneToMany(mappedBy = "author", cascade = ALL)
    private List<Article> articles = new ArrayList<>();

    protected User() {
    }

    private User(Email email, UserName userName, Password password) {
        this.email = email;
        this.profile = new Profile(userName);
        this.password = password;
    }

    public static User of(Email email, UserName userName, Password password) {
        return new User(email, userName, password);
    }

    public UserName getUserName() {
        return profile.getUserName();
    }

    public void update(User updatedUser) {
        if (hasText(updatedUser.getEmail().toString())) {
            this.email = updatedUser.getEmail();
        }
        if (hasText(updatedUser.getUserName().getNickname())) {
            this.profile = new Profile(updatedUser.getUserName());
        }
        if (hasText(updatedUser.getPassword().getValue())) {
            this.password = updatedUser.getPassword();
        }
    }

    public void addArticle(Article article) {
        articles.add(article);
    }

    public void removeArticle(Article article) {
        articles.remove(article);
    }
}
