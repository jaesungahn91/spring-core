# Spring Data JPA 고도화 플랜

## Context

Spring Data JPA 모듈을 "이 모듈을 제대로 이해했다"고 할 수 있을 수준으로 재설계한다.
현재 코드는 기본 CRUD + Value Object 정도만 커버하고 있어, 실무에서 자주 마주치는
고급 주제(N+1, 페이지네이션, 동적 쿼리, 낙관적 잠금, 소프트 삭제 등)가 빠져있다.

기존 코드는 참고 수준으로만 활용하고, 목적에 맞게 전면 재설계한다.

---

## 도메인 모델 (블로그 플랫폼)

기존 User + Article 구조를 확장해 다양한 JPA 관계를 자연스럽게 시연한다.

```
users (1) ──< articles (N)      → @OneToMany 양방향, cascade
users (1) ──< comments (N)      → @OneToMany 양방향
articles (1) ──< comments (N)   → @OneToMany 양방향, orphanRemoval
articles (N) >──< tags (M)      → @ManyToMany, @JoinTable
users (N) >──< users (M)        → Follow 별도 엔티티 (@EmbeddedId + @MapsId)
```

### 주요 필드 추가
- `User.version` → 낙관적 잠금 (`@Version`)
- `User/Article/Comment.deleted` → 소프트 삭제 (`@SQLDelete` + `@SQLRestriction`)
- `BaseEntity` → `@CreatedBy`, `@LastModifiedBy` 포함 (Auditing 심화)

---

## 커버 주제 vs 구현 위치

| 주제 | 파일 |
|---|---|
| @OneToMany 양방향 + cascade | `User`, `Article` |
| @ManyToMany + @JoinTable | `Article` ↔ `Tag` |
| @EmbeddedId + @MapsId 복합 PK | `Follow`, `FollowId` |
| fetch = LAZY + N+1 발생 시나리오 | `Article.author` (default EAGER → LAZY 수정) |
| JPQL JOIN FETCH로 N+1 해결 | `ArticleRepository.findByIdWithAuthor` |
| @EntityGraph로 N+1 해결 | `ArticleRepository.findAllWithAuthorAndTags` |
| 메서드 이름 파생 쿼리 | `UserRepository`, `ArticleRepository` |
| @Query JPQL | `ArticleRepository` 다수 |
| @Query native SQL | `ArticleRepository.findBySlugNative` |
| @Modifying 벌크 DML | `ArticleRepository.incrementViewCount`, `softDeleteByAuthorId` |
| Page<T> + Pageable | `ArticleRepository.findSummariesByPage` |
| Slice<T> (무한스크롤) | `ArticleRepository.findByAuthorOrderByCreatedAtDesc` |
| Interface-based Projection | `ArticleSummaryProjection` |
| DTO Projection (constructor) | `ArticleSummaryModel` + JPQL `new ...` |
| Specification + JpaSpecificationExecutor | `ArticleSpecification`, `ArticleRepository` |
| 커스텀 리포지토리 | `ArticleRepositoryCustom` + `ArticleRepositoryImpl` |
| @Version 낙관적 잠금 | `User`, `ArticleService.updateArticle` |
| @CreatedBy + AuditorAware | `BaseEntity`, `SecurityAuditorAware` |
| @SQLDelete + @SQLRestriction | `User`, `Article`, `Comment` |
| @DataJpaTest 슬라이스 테스트 | `ArticleRepositoryTest`, `UserRepositoryTest` |

---

## 네이밍 컨벤션 (실무 트렌드)

- **Request**: `CreateArticleRequest`, `UpdateUserRequest` (동사+명사+Request)
- **Response**: `ArticleResponse`, `ArticleSummaryResponse` (명사+Response)
- **모든 Request/Response는 Java record** 사용 (Java 17+, 불변 + 간결)
- **에러 응답**: `ProblemDetail` (RFC 9457, Spring 6 내장) — 별도 에러 DTO 불필요
- **글로벌 예외 처리**: `@RestControllerAdvice` + `@ExceptionHandler`
  - `EntityNotFoundException` → 404
  - `OptimisticLockException` → 409
  - `ConstraintViolationException` → 422

---

## 파일 구조 (최종)

```
spring-data-jpa/src/main/java/io/github/js/
├── SpringDataJpaApplication.java
├── application/
│   ├── user/
│   │   ├── UserRestController.java
│   │   ├── UserResponse.java               ← record (기존 UserModel 대체)
│   │   ├── CreateUserRequest.java          ← record (기존 UserPostRequestDto 대체)
│   │   └── UpdateUserRequest.java          ← record (기존 UserPutRequestDto 대체)
│   ├── article/
│   │   ├── ArticleRestController.java      ← 신규
│   │   ├── ArticleResponse.java            ← 신규 record
│   │   ├── ArticleSummaryResponse.java     ← 신규 record (DTO Projection)
│   │   ├── CreateArticleRequest.java       ← 신규 record
│   │   └── ArticleSearchCondition.java     ← 신규 record
│   ├── comment/
│   │   ├── CommentRestController.java      ← 신규
│   │   ├── CommentResponse.java            ← 신규 record
│   │   └── CreateCommentRequest.java       ← 신규 record
│   ├── follow/
│   │   ├── FollowRestController.java       ← 신규
│   │   └── FollowResponse.java             ← 신규 record
│   └── GlobalExceptionHandler.java         ← 신규 (@RestControllerAdvice + ProblemDetail)
├── domain/
│   ├── BaseEntity.java                     ← 수정 (@MappedSuperclass + 전체 Auditing)
│   ├── user/
│   │   ├── User.java                       ← 수정 (@Version, @SQLDelete, @OneToMany)
│   │   ├── Email.java, UserName.java, Password.java, Profile.java, Image.java
│   │   ├── UserRepository.java             ← 수정 (쿼리 메서드 추가)
│   │   └── UserService.java                ← 수정
│   ├── article/
│   │   ├── Article.java                    ← 수정 (BaseEntity 상속, @ManyToMany, @OneToMany, @SQLDelete)
│   │   ├── ArticleTitle.java, ArticleContents.java
│   │   ├── ArticleSummaryProjection.java   ← 신규 (Interface Projection)
│   │   ├── ArticleSpecification.java       ← 신규
│   │   ├── ArticleRepositoryCustom.java    ← 신규
│   │   ├── ArticleRepositoryImpl.java      ← 신규
│   │   ├── ArticleRepository.java          ← 수정 (대폭 확장)
│   │   └── ArticleService.java             ← 신규
│   ├── comment/
│   │   ├── Comment.java                    ← 신규
│   │   ├── CommentBody.java                ← 신규 (@Embeddable)
│   │   ├── CommentRepository.java          ← 신규
│   │   └── CommentService.java             ← 신규
│   ├── tag/
│   │   ├── Tag.java                        ← 신규
│   │   ├── TagName.java                    ← 신규 (@Embeddable)
│   │   └── TagRepository.java              ← 신규
│   └── follow/
│       ├── Follow.java                     ← 신규
│       ├── FollowId.java                   ← 신규 (@Embeddable 복합 PK)
│       ├── FollowRepository.java           ← 신규
│       └── FollowService.java              ← 신규
└── infrastructure/
    ├── repository/
    │   └── JpaConfig.java                  ← 기존 SpringDataJPAConfiguration 대체
    └── auditing/
        └── SecurityAuditorAware.java       ← 신규 (AuditorAware<String>)
```

```
spring-data-jpa/src/test/java/io/github/js/
├── acceptance/
│   ├── AcceptanceTest.java
│   ├── DatabaseCleanup.java                ← 수정 (신규 테이블 처리)
│   ├── user/UserAcceptanceTest.java
│   ├── article/ArticleAcceptanceTest.java  ← 신규
│   └── follow/FollowAcceptanceTest.java    ← 신규
├── application/
│   ├── user/UserRestControllerTest.java    ← 수정 (스켈레톤 채우기)
│   └── article/ArticleRestControllerTest.java ← 신규
└── domain/
    ├── article/ArticleRepositoryTest.java  ← 신규 (@DataJpaTest)
    ├── comment/CommentRepositoryTest.java  ← 신규 (@DataJpaTest)
    └── user/UserRepositoryTest.java        ← 신규 (@DataJpaTest)
```

---

## 이슈 워크플로우

각 Phase 시작 시 `issue-create` 스킬로 이슈 생성 → 작업 중 `issue-update` → Phase 완료 시 `issue-close`.

| Phase | 이슈 제목 | 라벨 |
|---|---|---|
| 1 | [feature] JPA 기반 인프라 구축 (BaseEntity, AuditorAware) | module:data-jpa |
| 2 | [feature] 신규 도메인 엔티티 추가 (Tag, Comment, Follow) | module:data-jpa |
| 3 | [feature] 기존 엔티티 고도화 (User, Article) | module:data-jpa |
| 4 | [feature] 리포지토리 계층 구현 (쿼리/프로젝션/Specification) | module:data-jpa |
| 5 | [feature] 서비스 계층 구현 | module:data-jpa |
| 6 | [feature] application 계층 구현 (Controller/Request/Response) | module:data-jpa |
| 7 | [feature] 테스트 구현 (@DataJpaTest, @WebMvcTest, E2E) | module:data-jpa |

---

## 구현 순서

### Phase 1 — 기반 인프라
1. `domain/BaseEntity.java` — `@MappedSuperclass`, `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`
2. `infrastructure/auditing/SecurityAuditorAware.java` — `AuditorAware<String>`, ThreadLocal 또는 고정값 반환
3. `infrastructure/repository/JpaConfig.java` — `@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")`
4. `application.properties` — `hibernate.generate_statistics=true` (테스트용)

### Phase 2 — 신규 도메인 엔티티
5. `domain/tag/TagName.java`, `Tag.java` — `@Entity`, `name` UNIQUE 제약
6. `domain/comment/CommentBody.java`, `Comment.java` — `@SQLDelete`, `@SQLRestriction`, `BaseEntity` 상속
7. `domain/follow/FollowId.java` — `@Embeddable`, `Serializable`
8. `domain/follow/Follow.java` — `@EmbeddedId`, `@MapsId`, `@ManyToOne` follower/followee

### Phase 3 — 기존 엔티티 고도화
9. `domain/user/User.java` — `@Version`, `@SQLDelete("UPDATE users SET deleted=true WHERE id=?")`, `@SQLRestriction("deleted=false")`, `@OneToMany(mappedBy="author", cascade=ALL)` articles
10. `domain/article/Article.java` — `BaseEntity` 상속 (기존 `@EntityListeners` 제거), `fetch=LAZY` 명시, `@ManyToMany` tags, `@OneToMany` comments, `@SQLDelete`, `@SQLRestriction`, `viewCount` 필드 추가

### Phase 4 — 리포지토리 계층
11. `domain/article/ArticleSummaryProjection.java` — Interface Projection (`@Value` SpEL)
12. `domain/article/ArticleRepositoryCustom.java` + `ArticleRepositoryImpl.java` — EntityManager 직접 사용
13. `domain/article/ArticleSpecification.java` — `hasAuthorName`, `hasTag`, `createdAfter`
14. `domain/article/ArticleRepository.java` — 아래 목록 전체:
    - `findByIdWithAuthor` (JOIN FETCH)
    - `findAllWithAuthorAndTags` (@EntityGraph)
    - `findBySlugNative` (native query)
    - `incrementViewCount` (@Modifying clearAutomatically=true)
    - `softDeleteByAuthorId` (@Modifying)
    - `findSummariesByPage` (DTO Projection + Page)
    - `findByAuthorOrderByCreatedAtDesc` (Slice)
    - `findByAuthorId` (Interface Projection)
    - `JpaSpecificationExecutor<Article>` extends
15. `domain/comment/CommentRepository.java` — `findByArticleIdWithAuthor` (JOIN FETCH)
16. `domain/follow/FollowRepository.java` — `existsByFollowerIdAndFolloweeId`, `findByFollowerId`, `countByFolloweeId`
17. `domain/user/UserRepository.java` — `findByEmail`, `existsByEmail`, `findByUsername` (@Query JPQL)
18. `domain/tag/TagRepository.java`

### Phase 5 — 서비스 계층
19. `domain/article/ArticleService.java` — Specification 활용, @Modifying 호출, 낙관적 잠금 처리
20. `domain/comment/CommentService.java`
21. `domain/follow/FollowService.java`
22. `domain/user/UserService.java` — 기존 대비 소프트 삭제 추가

### Phase 6 — application 계층
23. `application/article/ArticleSummaryResponse.java` — DTO Projection용 record (id, title, authorName, createdAt)
24. `application/article/ArticleResponse.java`, `CreateArticleRequest.java`, `ArticleSearchCondition.java` (모두 record)
25. `application/article/ArticleRestController.java`:
    - `POST /articles`
    - `GET /articles/{id}`
    - `GET /articles?page=0&size=10&sort=createdAt,desc` → Pageable 자동 바인딩
    - `GET /articles/search?author=john&tag=java` → Specification
    - `GET /articles/feed?page=0&size=5` → Slice (hasNext 포함 응답)
    - `PUT /articles/{id}` → 낙관적 잠금 409 처리
    - `DELETE /articles/{id}` → 소프트 삭제
    - `POST /articles/{id}/view` → 벌크 viewCount 증가
26. `application/GlobalExceptionHandler.java` — `ProblemDetail` 기반 에러 응답 (EntityNotFoundException→404, OptimisticLockException→409)
27. `application/comment/`, `application/follow/` — record 기반 Request/Response, 기본 CRUD

### Phase 7 — 테스트
27. `domain/article/ArticleRepositoryTest.java` (@DataJpaTest):
    - JOIN FETCH vs 일반 조회 쿼리 수 비교 (Hibernate statistics)
    - N+1 발생 시나리오 명시 (comments 로딩 후 각 author 접근)
    - @EntityGraph 적용 후 단일 쿼리 확인
    - Page<T> → COUNT 쿼리 실행 검증, Slice<T> → COUNT 없음 검증
    - DTO Projection, Interface Projection 값 검증
    - Specification 필터링 검증
    - @Modifying 후 clearAutomatically 없으면 stale 발생 시연
    - 소프트 삭제 후 @SQLRestriction으로 조회 제외 검증
28. `domain/user/UserRepositoryTest.java` (@DataJpaTest):
    - 낙관적 잠금 `OptimisticLockException` 발생 시나리오
    - @CreatedBy/@LastModifiedBy 자동 세팅 검증
29. `domain/comment/CommentRepositoryTest.java` (@DataJpaTest)
30. `application/user/UserRestControllerTest.java` (@WebMvcTest) — 유효성 검증 실패/성공 케이스
31. `application/article/ArticleRestControllerTest.java` (@WebMvcTest) — Pageable 바인딩, 409 응답
32. `acceptance/DatabaseCleanup.java` — `article_tags`, `follows` 테이블 추가, ID 컬럼 없는 테이블은 ID RESTART 스킵
33. `acceptance/article/ArticleAcceptanceTest.java` — 생성/조회/페이지네이션/소프트삭제 시나리오
34. `acceptance/follow/FollowAcceptanceTest.java` — 팔로우/언팔로우/목록 시나리오

---

## 주요 설계 결정

### @SQLRestriction vs @Where
- Hibernate 6 (Spring Boot 3.x)에서 `@Where`는 deprecated
- `@SQLRestriction("deleted = false")` 사용
- `@Query native`에는 `AND deleted = false` 명시 필요 (자동 적용 안 됨)

### @ManyToMany cascade 전략
- `cascade = {PERSIST, MERGE}` (`REMOVE` 제외) — Tag는 독립 생명주기
- Article 소프트 삭제 시 `article_tags` 레코드는 물리 유지

### Follow 별도 엔티티
- User 내 `@ManyToMany` 자기 참조 대신 Follow 엔티티로 분리
- `createdAt` 필드 추가 가능 + `@EmbeddedId + @MapsId` 패턴 시연 목적

### @Modifying + clearAutomatically
- 모든 `@Modifying`에 `clearAutomatically = true, flushAutomatically = true` 적용
- 테스트에서 이 옵션 없을 때의 stale 문제를 명시적으로 시연

### DatabaseCleanup 개선
- `article_tags`, `follows`는 별도 ID 컬럼 없음 → `noIdResetTables` Set으로 분리
- `TRUNCATE`는 그대로 실행, `ALTER COLUMN ID RESTART` 는 스킵

### Naming convention
- Request/Response는 `*Request` / `*Response` suffix, Java record 사용
- 클래스명 앞에 동사 포함: `Create*Request`, `Update*Request`
- 에러 응답은 별도 클래스 없이 Spring 내장 `ProblemDetail` 활용

---

## 검증 방법

1. `./gradlew :spring-data-jpa:test` — 전체 테스트 통과 확인
2. `./gradlew :spring-data-jpa:jacocoTestReport` — 커버리지 확인
3. 애플리케이션 기동 후 `POST /users` → `POST /articles` → `GET /articles?page=0&size=5&sort=createdAt,desc` 호출
4. `ArticleRepositoryTest` 실행 후 로그에서 N+1 쿼리 수 vs JOIN FETCH 쿼리 수 비교 확인
5. Postman 컬렉션 업데이트 후 `run-api-tests.sh` 실행
