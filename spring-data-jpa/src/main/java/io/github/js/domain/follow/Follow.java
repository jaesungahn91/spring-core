package io.github.js.domain.follow;

import io.github.js.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

import static jakarta.persistence.FetchType.LAZY;

/**
 * 복합 PK + @MapsId 시연:
 * - @EmbeddedId: 복합 PK를 Embeddable 객체로 표현
 * - @MapsId: 연관관계의 FK를 복합 PK의 특정 필드에 매핑
 *
 * Follow 별도 엔티티로 분리한 이유:
 * - createdAt 같은 관계 속성 추가 가능
 * - User 내 @ManyToMany 자기 참조보다 의도가 명확
 */
@Getter
@Entity
@Table(name = "follows")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Follow {

    @EmbeddedId
    private FollowId id;

    @ManyToOne(fetch = LAZY)
    @MapsId("followerId")
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne(fetch = LAZY)
    @MapsId("followeeId")
    @JoinColumn(name = "followee_id")
    private User followee;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    private Follow(User follower, User followee) {
        this.id = FollowId.of(follower.getId(), followee.getId());
        this.follower = follower;
        this.followee = followee;
    }

    public static Follow of(User follower, User followee) {
        return new Follow(follower, followee);
    }
}
