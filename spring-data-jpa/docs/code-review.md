# Code Review — spring-data-jpa

## Summary

전반적으로 학습용 레퍼런스 목적에 충실하게 설계되어 있습니다. 커버하려는 JPA 주제들이 코드와 주석, 테스트까지 일관성 있게 연결되어 있으며, 대부분의 패턴이 의도한 대로 시연됩니다. 다만 몇 가지 동작 정확성 문제와 개념적 오류가 있어 학습 자료로서 오해를 유발할 수 있는 부분을 중점적으로 지적합니다.

---

## Critical (P0)

**`@SQLDelete`에 `version` 파라미터 미적용 — 소프트 삭제 시 낙관적 잠금 우회**

- `domain/article/Article.java:41`
- `Article`의 `@SQLDelete`가 `WHERE id = ?`만 사용합니다. `User`와 달리 `version` 조건이 없어 동시 수정 상황에서 낙관적 잠금이 동작하지 않습니다. `User`는 `WHERE id = ? AND version = ?`로 올바르게 작성되어 있으나, `Article`과 `Comment`는 `version` 컬럼도 없습니다. 두 엔티티의 `@SQLDelete` 형식이 다른 이유가 주석에 없으면 혼란을 야기합니다.

**`ArticleSummaryProjection` — SpEL 기반 Interface Projection이 N+1을 유발함에도 경고 부족**

- `domain/article/ArticleSummaryProjection.java:23`
- `getAuthorName()`이 `#{target.author.profile.userName.nickname}`으로 정의되어 있습니다. SpEL 기반 Interface Projection은 전체 엔티티를 로딩한 뒤 SpEL로 접근하므로 LAZY author를 N번 로딩합니다. 학습 자료라면 이 방식의 한계를 보여주고, Closed Projection 또는 DTO Projection으로 교체하는 예제가 함께 있어야 합니다.

**`UpdateUserRequest.toEntity()`가 null 값으로 VO를 생성함**

- `application/user/UpdateUserRequest.java:13`
- 모든 필드가 nullable인데 `toEntity()`에서 `new Email(null)`, `new UserName(null)`을 그대로 전달합니다. `User.update()`에서 `hasText()`로 방어하고 있으나, VO 생성자 자체에 검증이 없어 의도치 않은 null이 Embedded 필드에 저장될 위험이 있습니다.

---

## Major (P1)

**`ArticleTitle.slugFromTitle()` 정규식 오류**

- `domain/article/ArticleTitle.java:33`
- `"\\$,'\"|\\s|\\.|\\?"` 패턴이 alternation(`|`)으로 작성되어 `$,'"` 전체가 하나의 분기로 처리됩니다. `"[\\s.,?$'\"]"`와 같이 문자 클래스로 작성해야 합니다.

**`hasTag` Specification이 INNER JOIN을 생성해 `Page` COUNT 쿼리와 충돌 가능**

- `domain/article/ArticleSpecification.java:35`
- `root.join("tags")`는 기본적으로 INNER JOIN을 생성합니다. 컬렉션 JOIN이 포함된 Specification과 `Page`를 조합하면 COUNT 쿼리와 SELECT 쿼리의 JOIN이 달라 결과가 부정확해질 수 있습니다. `countQuery`를 별도로 제공하거나 `EXISTS` 서브쿼리 방식이 실무에 가깝습니다.

**`ArticleService.searchArticles()`에서 엔티티 필드 접근 시 N+1 발생**

- `domain/article/ArticleService.java:86-88`
- `a.getAuthor().getProfile().getUserName().getNickname()`과 같이 LAZY 연관관계를 엔티티에서 직접 접근하므로 N+1이 발생합니다. `findSummariesByPage`는 DTO Projection으로 해결했는데, `searchArticles`는 엔티티를 가져온 뒤 매핑하는 방식이라 일관성이 없습니다.

**`FollowRepository` 조회 메서드에 JOIN FETCH 없음**

- `domain/follow/FollowRepository.java:12-13`
- `FollowResponse.from(Follow)`가 `follow.getFollowee().getUserName()` 등 연관 엔티티에 접근합니다. `findByFollowerId`가 Follow 목록을 가져온 뒤 각 `follower`/`followee`를 LAZY 로딩하므로 N+1이 발생합니다. N+1 해결을 핵심 주제로 다루는 모듈인 만큼, `@EntityGraph` 또는 JPQL JOIN FETCH 적용 또는 의도적 미해결 케이스로 명시해야 합니다.

**`User.@OneToMany(cascade = ALL)`에 `orphanRemoval` 누락**

- `domain/user/User.java:52`
- `Article`의 `@OneToMany(comments)`는 `orphanRemoval = true`가 있는 반면, `User.articles`에는 없습니다. 소프트 삭제 엔티티이므로 의도적 설계일 수 있으나, 그 이유가 주석에 없어 두 `@OneToMany` 설정 간 불일치가 혼란을 줍니다.

**`CommentService.addComment()`에서 `cascade` 동작과 `save()` 중복 호출**

- `domain/comment/CommentService.java:30-31`
- `article.addComment(comment)` 호출 후 `commentRepository.save(comment)`를 명시적으로 호출합니다. `Article`이 `cascade = ALL`을 가지고 있어 이미 영속 상태의 `article`에 추가된 `comment`는 자동 저장됩니다. 학습 자료에서 cascade 동작과 명시적 save의 차이를 설명하지 않으면 의도가 불명확합니다.

**`SecurityAuditorAware` ThreadLocal 미해제 위험**

- `infrastructure/auditing/SecurityAuditorAware.java:16`
- 스레드 풀 환경에서 `set()` 후 `clear()`를 호출하지 않으면 이전 요청의 auditor가 다음 요청에 유출됩니다. 실제 HTTP 요청 처리에서는 `Filter` 또는 `HandlerInterceptor`에서 `try-finally`로 보장해야 함을 주석에 명시해야 합니다.

**`PUT /users/{id}` 엔드포인트에 `@Valid` 누락**

- `application/user/UserRestController.java:33`
- `UpdateUserRequest`에 `@Valid`가 없어 빈 문자열이 통과해 Embedded VO에 저장될 수 있습니다. 의도된 설계라면 주석으로 명시가 필요합니다.

---

## Minor

**`ArticleTitle.slugFromTitle()` — null 입력 방어 없음**

- `domain/article/ArticleTitle.java:33`
- `title`이 null이면 NullPointerException이 발생합니다. VO 생성자에서 검증이 있어야 합니다.

**`DatabaseCleanup.TABLE_NAME_OVERRIDES` — 엔티티 추가 시 수동 업데이트 필요**

- `test/.../DatabaseCleanup.java:35`
- `@Table(name = "follows")` 등을 직접 읽거나 JPA 메타모델에서 `@Table` 어노테이션을 파싱하는 방식이 더 견고합니다. 현재 구조는 엔티티가 추가될 때마다 수동 업데이트가 필요합니다.

**`UserRepositoryTest.createdByIsSetOnSave()` — 테스트 목적이 불명확**

- `test/.../UserRepositoryTest.java:80-95`
- 주석에 "User는 BaseEntity를 상속하지 않으므로 Article로 테스트"라고 되어 있으나 실제로는 `User`만 저장하고 `id != null`만 확인합니다. `@CreatedBy` 동작을 검증하려면 `BaseEntity`를 상속한 `Article`로 테스트해야 합니다.

**`ArticleRepository.findAll()` 재선언이 불필요**

- `domain/article/ArticleRepository.java:30`
- N+1 시연 목적으로 `findAll()`을 재선언하고 있으나, 이는 `JpaRepository.findAll()`과 동일한 시그니처라 실질적 차이가 없습니다. 인터페이스 선언을 제거하고 테스트에서 `findAll()`을 직접 호출하는 방식이 더 명확합니다.

**`TagName` UNIQUE 제약과 동시성 미언급**

- `domain/tag/TagName.java:16`
- 동시에 같은 태그명으로 `Article`을 생성하는 요청이 들어오면 UNIQUE 제약 위반이 발생할 수 있습니다. 학습용 허용 범위이나 "동시성 고려 시 별도 잠금 필요" 주석을 추가하면 좋습니다.

---

## Good Practices

- N+1 발생 → JOIN FETCH → `@EntityGraph` 순서로 동일 문제를 점진적으로 해결하는 구성이 학습 흐름에 적합합니다.
- `@DataJpaTest` / `@WebMvcTest` / REST Assured E2E 세 계층으로 테스트를 분리한 구조가 명확합니다.
- `ArticleSpecification`의 null-safe 처리(`cb.conjunction()` 반환)가 일관성 있게 적용되어 있습니다.
- `Follow` 엔티티를 `@EmbeddedId` + `@MapsId`로 모델링하고 별도 엔티티로 분리한 선택이 실무 관점에서 올바릅니다.
- `FollowId`가 `Serializable`을 구현하고 `equals`/`hashCode`를 명시적으로 정의한 것이 JPA 복합키 필수 조건을 정확히 따릅니다.
- `UserRepositoryTest`의 낙관적 잠금 테스트가 두 개의 독립적인 `EntityManager`를 사용해 실제 충돌 시나리오를 재현한 점이 좋습니다.
- `ProblemDetail` (RFC 9457) 기반 에러 응답 구조가 Spring 6 표준을 잘 따릅니다.
- `SecurityAuditorAware`에서 Spring Security 없이 ThreadLocal로 대체한 것을 명시한 점이 모듈 목적에 적합합니다.
