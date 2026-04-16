package io.github.js.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 메서드 이름 파생 쿼리: Email Embeddable의 address 필드로 조회
     * → SELECT u FROM User u WHERE u.email = :email
     */
    Optional<User> findByEmail(Email email);

    /**
     * exists 파생 쿼리: COUNT 대신 EXISTS로 최적화된 중복 확인
     */
    boolean existsByEmail(Email email);

    /**
     * @Query JPQL: Embedded 중첩 경로(profile.userName.value) 접근
     * 메서드 이름 파생으로 표현하면 findByProfileUserNameValue()처럼 가독성이 떨어지므로 @Query 사용
     */
    @Query("SELECT u FROM User u WHERE u.profile.userName.nickname = :username")
    Optional<User> findByUsername(@Param("username") String username);
}
