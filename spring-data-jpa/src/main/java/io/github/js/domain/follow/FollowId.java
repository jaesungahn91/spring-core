package io.github.js.domain.follow;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 복합 PK 시연:
 * - @Embeddable: 복합 키 클래스는 반드시 Serializable 구현
 * - equals/hashCode: JPA 동일성 판단에 필수
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowId implements Serializable {

    private Long followerId;
    private Long followeeId;

    public static FollowId of(Long followerId, Long followeeId) {
        FollowId id = new FollowId();
        id.followerId = followerId;
        id.followeeId = followeeId;
        return id;
    }
}
