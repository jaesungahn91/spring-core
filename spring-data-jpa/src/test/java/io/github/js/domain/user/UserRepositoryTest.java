package io.github.js.domain.user;

import io.github.js.infrastructure.auditing.SecurityAuditorAware;
import io.github.js.infrastructure.config.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.RollbackException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaConfig.class, SecurityAuditorAware.class})
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

    @Autowired UserRepository userRepository;
    @PersistenceContext EntityManager em;
    @PersistenceUnit EntityManagerFactory emf;

    // -------------------------------------------------------------------------
    // 낙관적 잠금
    // -------------------------------------------------------------------------

    /**
     * 낙관적 잠금 충돌 시나리오를 검증하기 위해 두 개의 독립적인 EntityManager를 사용한다.
     * 같은 EntityManager 내에서는 동일 PK에 대해 항상 같은 인스턴스를 반환하므로
     * (1차 캐시 / Identity Map), 동일 em으로는 충돌을 재현할 수 없다.
     */
    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DisplayName("낙관적 잠금: 두 세션이 같은 엔티티를 로드 후 먼저 커밋한 쪽이 version을 올리면 늦은 쪽은 커밋 실패")
    void optimisticLockThrowsOnConflict() {
        // Step 1: 엔티티 저장
        Long savedId;
        EntityManager setup = emf.createEntityManager();
        setup.getTransaction().begin();
        User saved = User.of(new Email("lock@test.com"), new UserName("locker"), new Password("pw"));
        setup.persist(saved);
        setup.getTransaction().commit();
        savedId = saved.getId();
        setup.close();

        // Step 2: 세션 A — version=0 으로 로드 (아직 커밋 안 함)
        EntityManager sessionA = emf.createEntityManager();
        sessionA.getTransaction().begin();
        User userA = sessionA.find(User.class, savedId);

        // Step 3: 세션 B — 같은 엔티티를 로드, 수정, 커밋 → DB version=1
        EntityManager sessionB = emf.createEntityManager();
        sessionB.getTransaction().begin();
        User userB = sessionB.find(User.class, savedId);
        userB.update(User.of(new Email("b@test.com"), new UserName("userB"), new Password("pw")));
        sessionB.getTransaction().commit();
        sessionB.close();

        // Step 4: 세션 A — version=0 상태로 수정 후 커밋 시도 → DB version(1)과 불일치 → 예외
        userA.update(User.of(new Email("a@test.com"), new UserName("userA"), new Password("pw")));
        assertThatThrownBy(() -> sessionA.getTransaction().commit())
                .isInstanceOfAny(OptimisticLockException.class, RollbackException.class);
        sessionA.close();
    }

    // -------------------------------------------------------------------------
    // JPA Auditing
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("@CreatedBy: 저장 시 SecurityAuditorAware에서 auditor 자동 세팅")
    void createdByIsSetOnSave() {
        SecurityAuditorAware.set("john");

        User user = userRepository.save(
                User.of(new Email("audit@test.com"), new UserName("auditor"), new Password("pw")));
        em.flush();
        em.clear();

        User found = userRepository.findById(user.getId()).orElseThrow();
        assertThat(found.getCreatedBy()).isEqualTo("john");
        assertThat(found.getCreatedAt()).isNotNull();

        SecurityAuditorAware.clear();
    }

    @Test
    @DisplayName("소프트 삭제: delete 후 findById 결과 없음, DB에는 deleted=true")
    void softDeleteMarksAsDeleted() {
        User user = userRepository.save(
                User.of(new Email("soft@test.com"), new UserName("softuser"), new Password("pw")));
        em.flush();
        em.clear();

        userRepository.delete(user);
        em.flush();
        em.clear();

        assertThat(userRepository.findById(user.getId())).isEmpty();

        Object deleted = em.createNativeQuery(
                "SELECT deleted FROM users WHERE id = " + user.getId())
                .getSingleResult();
        assertThat(deleted).isEqualTo(true);
    }

    @Test
    @DisplayName("existsByEmail: 중복 이메일 EXISTS 쿼리로 확인")
    void existsByEmailReturnsTrueForDuplicate() {
        Email email = new Email("dup@test.com");
        userRepository.save(User.of(email, new UserName("user1"), new Password("pw")));
        em.flush();

        assertThat(userRepository.existsByEmail(email)).isTrue();
        assertThat(userRepository.existsByEmail(new Email("notexist@test.com"))).isFalse();
    }
}
