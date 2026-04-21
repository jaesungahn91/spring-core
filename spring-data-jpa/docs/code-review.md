# Code Review — spring-data-jpa

작성일: 2026-04-17

---

## 1. 모듈 개요

### 목적

Spring Data JPA의 핵심 기능을 실습 목적으로 구현한 학습 모듈. 실제 서비스가 아니라 JPA 개념 시연(N+1, 낙관적 잠금, 소프트 삭제, 페이지네이션, 프로젝션 등)에 초점을 둔다.

### 주요 도메인

- `User` — 회원, 소프트 삭제, 낙관적 잠금(`@Version`), Auditing
- `Article` — 게시글, 소프트 삭제, 태그 관계(`@ManyToMany`), 댓글 관계(`@OneToMany`)
- `Comment` — 댓글, 소프트 삭제
- `Tag` — 태그, `@ManyToMany` 반대편
- `Follow` — 복합 PK(`@EmbeddedId + @MapsId`), 팔로우 관계

### 레이어 구성

```
application/   — Controller, Request/Response DTO, GlobalExceptionHandler
domain/        — Entity, Repository, Service, Value Object
infrastructure/ — JpaConfig, SecurityAuditorAware
```

---

## 2. 아키텍처 분석

### 레이어 간 의존성

- `domain/` 패키지에 `Service`와 `Repository`가 함께 위치한다. 도메인 객체와 리포지토리 인터페이스를 같은 패키지에 두는 방식 자체는 DDD 스타일에서 허용되지만, `ArticleService`가 `application` 레이어의 `ArticleSummaryResponse`를 직접 import(`src/main/java/io/github/js/domain/article/ArticleService.java:4`)하고 있어 도메인→애플리케이션 방향의 역방향 의존이 발생한다.
- `ArticleRepository`도 동일하게 `ArticleSummaryResponse`를 import(`src/main/java/io/github/js/domain/article/ArticleRepository.java:4`)한다. 리포지토리가 응답 DTO를 직접 참조하는 구조다.

### 패키지 구조

- `application/` 하위가 도메인별(article, comment, follow, user)로 분리되어 있어 구조는 명확하다.
- `infrastructure/`는 `auditing`과 `repository`(JpaConfig만 존재)로 구성되어 있으나, `JpaConfig`는 설정 클래스이므로 `config/` 패키지로 이동하는 것이 관용적이다. 현재 JaCoCo 제외 패턴(`**/config/**`)과도 불일치한다(`build.gradle:55`).

---

## 3. 코드 품질

### 도메인 레이어

**Entity**

- `User`는 `BaseEntity`를 상속하지 않아 `createdAt`, `updatedAt`이 없다. `Article`, `Comment`와 달리 Auditing 필드가 누락되어 일관성이 깨진다(`src/main/java/io/github/js/domain/user/User.java:26`).
- `User.update()` 메서드가 `hasText(updatedUser.getEmail().toString())`로 null 체크를 하지만, `UpdateUserRequest.toEntity()`는 입력값에 대한 검증 없이 `Email`, `UserName`, `Password` VO를 생성한다(`src/main/java/io/github/js/application/user/UpdateUserRequest.java:13`). 빈 문자열이나 null이 그대로 VO 생성자에 전달된다.
- `User.update()` 내부에서 이메일만 갱신하고 `UserName`, `Password` 변경은 처리하지 않는다(`src/main/java/io/github/js/domain/user/User.java:72-76`). `UpdateUserRequest`는 세 필드를 모두 받지만 실제 반영은 이메일만 된다.
- `Article`에 `cascade = ALL`이 `comments`에 적용되어 있고 `orphanRemoval = true`도 함께 설정되어 있다. `cascade = ALL`에는 이미 `REMOVE`가 포함되므로 `orphanRemoval`의 추가 효과는 컬렉션에서 직접 제거할 때만 의미 있다. 의도는 맞지만 주석과 코드 간 설명이 혼재되어 있다(`src/main/java/io/github/js/domain/article/Article.java:78`).
- `ArticleTitle.slugFromTitle()`의 정규식(`\\$,'\"|\\s|\\.|\\?`)은 `$`, `,`, `'`, `"`, 공백, `.`, `?`만 처리한다. 한글, 특수문자 등은 슬러그에 그대로 남는다(`src/main/java/io/github/js/domain/article/ArticleTitle.java:33`). 이 모듈이 학습 목적이라면 허용 범위지만 실용적이지 않다.

**Value Object**

- `Email`에 형식 검증이 없다. VO 생성 시 유효하지 않은 이메일도 허용된다(`src/main/java/io/github/js/domain/user/Email.java:18`).
- `Password`에 길이/복잡도 검증 및 인코딩 로직이 없다. 평문이 그대로 저장된다(`src/main/java/io/github/js/domain/user/Password.java:13`). 학습 모듈이므로 의도적일 수 있으나 주석으로 명시되어 있지 않다.
- `CommentBody`, `TagName` 등 다른 VO에는 최소한의 null 가드나 길이 제약이 없다.

### 리포지토리 레이어

- `ArticleRepository.findSummariesByPage()`의 JPQL 생성자 표현식이 `application` 패키지의 `ArticleSummaryResponse`를 전체 경로로 참조한다(`src/main/java/io/github/js/domain/article/ArticleRepository.java:88`). 리포지토리가 응답 DTO를 직접 의존하는 구조로, 도메인 레이어의 순수성이 훼손된다.
- `ArticleSummaryProjection`의 `@Value SpEL` 표현식(`#{target.author.profile.userName.nickname}`)은 author가 LAZY로 로딩될 경우 추가 쿼리가 발생한다. 인터페이스 상단 주석에 언급되어 있지만 호출 측(`findProjectionsByAuthorId`)에서 JOIN FETCH 처리가 없다(`src/main/java/io/github/js/domain/article/ArticleRepository.java:107`).
- `ArticleSpecification.hasTag()`는 `root.join("tags")`를 사용하여 INNER JOIN을 수행한다. 태그 조건이 없을 때는 `cb.conjunction()`을 반환하지만, 태그가 있을 경우 다중 태그 조인 시 중복 결과가 발생할 수 있다. `Specification`을 `Page`로 조회할 때 count 쿼리에도 JOIN이 포함되어 성능에 영향을 줄 수 있다(`src/main/java/io/github/js/domain/article/ArticleSpecification.java:35`).

### 서비스 레이어

- `ArticleService.createArticle()`에서 태그를 하나씩 `tagRepository.findByName()`으로 조회한다(`src/main/java/io/github/js/domain/article/ArticleService.java:37-41`). 태그가 N개이면 N번의 SELECT가 발생한다. `TagRepository.findByNameIn()`이 이미 존재하므로 일괄 조회로 개선 가능하다.
- `ArticleService.searchArticles()`가 서비스 내에서 직접 `ArticleSummaryResponse` 생성자를 호출하며 `a.getAuthor().getProfile().getUserName().getNickname()`을 접근한다(`src/main/java/io/github/js/domain/article/ArticleService.java:84-88`). `Specification`으로 조회된 `Article`의 `author`가 LAZY 로딩되므로 N+1이 발생한다. `findSummariesByPage()`와 달리 JOIN FETCH가 적용되지 않는다.
- `UserService.findById()`가 `Optional<User>`를 반환하여 컨트롤러가 직접 `orElse(ResponseEntity.notFound().build())`를 처리한다(`src/main/java/io/github/js/application/user/UserRestController.java:26-30`). 예외 처리 방식이 다른 엔드포인트(`EntityNotFoundException` 방식)와 일관되지 않는다.
- `CommentService.addComment()`에서 `article.addComment(comment)` 호출 후 `commentRepository.save(comment)`를 별도로 호출한다(`src/main/java/io/github/js/domain/comment/CommentService.java:29-31`). `Article.comments`에 `cascade = ALL`이 설정되어 있으므로 `article.addComment()`만으로도 cascade를 통해 저장이 가능하다. 현재 코드는 동작하지만 의도가 혼재되어 있다.

### 컨트롤러 레이어

- `ArticleRestController.createArticle()`과 `CommentRestController.addComment()`에서 인증된 사용자 ID를 `@RequestParam`으로 받는다(`src/main/java/io/github/js/application/article/ArticleRestController.java:26`, `src/main/java/io/github/js/application/comment/CommentRestController.java:22`). 이는 클라이언트가 임의의 `authorId`를 전달할 수 있으므로 인증이 있는 실제 서비스에서는 보안 취약점이다. 학습 모듈이지만 주석으로 한계를 명시하는 것이 적절하다.
- `FollowRestController`와 `UserRestController`가 모두 `@RequestMapping("/users")`를 사용한다(`src/main/java/io/github/js/application/follow/FollowRestController.java:12`, `src/main/java/io/github/js/application/user/UserRestController.java:13`). URL 구조상 충돌은 없지만, 같은 URL 루트를 두 컨트롤러가 나눠 가지는 구조는 유지보수 시 혼란을 줄 수 있다.
- `ArticleRestController.updateArticle()`이 `CreateArticleRequest`를 재사용한다(`src/main/java/io/github/js/application/article/ArticleRestController.java:82`). 생성과 수정의 요청 구조가 다를 경우 분리가 필요하다.

---

## 4. 테스트 분석

### 테스트 전략

3계층으로 구성되어 있다.

| 계층 | 어노테이션 | 대상 |
|------|-----------|------|
| Repository | `@DataJpaTest` | `ArticleRepositoryTest`, `UserRepositoryTest`, `CommentRepositoryTest` |
| Controller | `@WebMvcTest` | `ArticleRestControllerTest`, `UserRestControllerTest` |
| E2E | `@SpringBootTest` + RestAssured | `ArticleAcceptanceTest`, `UserAcceptanceTest`, `FollowAcceptanceTest` |

### 커버리지 (JaCoCo, 라인 기준)

| 패키지 | 커버율 |
|--------|--------|
| `application` (GlobalExceptionHandler) | 100% |
| `application/follow` | 100% |
| `application/user` | 89% |
| `application/article` | 79% |
| `application/comment` | 0% |
| `domain/follow` | 91% |
| `domain/tag` | 100% |
| `domain/user` | 75% |
| `domain/article` | 72% |
| `domain/comment` | 48% |
| 전체 (라인) | 228/306 (75%) |

### 품질 이슈

- `application/comment` 패키지 커버율이 0%다. `CommentRestController`에 대한 `@WebMvcTest`가 존재하지 않는다.
- `domain/comment` 커버율이 48%로 낮다. `CommentService`에 대한 단위 테스트가 없고 `CommentRepositoryTest`만 존재한다.
- `UserRepositoryTest.createdByIsSetOnSave()`는 `User`가 `BaseEntity`를 상속하지 않는다는 사실을 테스트 내 주석으로 인정하면서 `Article`로 검증해야 한다고 언급하지만, 실제로는 `User.id` not null만 확인하고 종료된다(`src/test/java/io/github/js/domain/user/UserRepositoryTest.java:90-93`). 테스트 명칭(`@CreatedBy: 저장 시 SecurityAuditorAware에서 auditor 자동 세팅`)과 실제 검증 내용이 불일치한다.
- `ArticleRepositoryTest.nPlusOneProblem()`은 N+1이 발생함을 확인하지만 `assertThat(queryCount).isGreaterThan(1)`로만 단언한다(`src/test/java/io/github/js/domain/article/ArticleRepositoryTest.java:79`). 정확한 쿼리 수를 검증하지 않아 Hibernate의 배치 페칭이나 1차 캐시로 인해 실제 N+1이 발생하지 않아도 테스트가 통과할 수 있다.
- `DatabaseCleanup`이 `@ActiveProfiles("test")`를 선언하지만 실제로 `application-test.properties`가 없고 별도 프로파일 분기가 없다. 어노테이션이 동작에 영향을 주지 않는다(`src/test/java/io/github/js/acceptance/DatabaseCleanup.java:17`).
- `FollowAcceptanceTest`에서 두 번째 사용자 생성 메서드(`createAnotherUser`)가 전체 패키지 경로를 inline으로 작성한다(`src/test/java/io/github/js/acceptance/follow/FollowAcceptanceTest.java:69-72`). 불필요한 중복이며 `UserAcceptanceTest`의 헬퍼 메서드를 활용하거나 공통 팩토리로 추출하는 것이 적절하다.

---

## 5. 개선 제안

### High

- **`ArticleService.searchArticles()` N+1** (`domain/article/ArticleService.java:83-88`)
  - `Specification` 조회 후 `author`를 LAZY 로딩으로 접근하여 N+1이 발생한다.
  - `findSummariesByPage()`처럼 JPQL 생성자 표현식 또는 `@EntityGraph`를 적용하거나, `ArticleRepositoryCustom`으로 이동하여 JOIN FETCH 쿼리를 작성한다.

- **도메인→애플리케이션 역방향 의존 제거** (`domain/article/ArticleService.java:4`, `domain/article/ArticleRepository.java:4`)
  - `ArticleSummaryResponse`를 도메인 레이어에서 분리하려면 도메인 전용 DTO(예: `ArticleSummary`)를 `domain/article` 패키지에 두고, 컨트롤러 응답 변환을 `application` 레이어에서 담당하게 한다.
  - 또는 `ArticleSummaryResponse`를 `domain/article`로 이동하여 방향을 정리한다.

- **`UpdateUserRequest` 유효성 검증 및 `User.update()` 불완전 구현** (`application/user/UpdateUserRequest.java`, `domain/user/User.java:72`)
  - 수정 요청 필드에 검증 어노테이션(`@Email`, `@NotBlank` 등) 적용이 필요하다.
  - `User.update()`가 이메일만 반영하고 `UserName`, `Password` 변경을 누락한다. 메서드 시그니처 또는 구현 범위를 명확히 해야 한다.

### Medium

- **`User`의 `BaseEntity` 미상속** (`domain/user/User.java:26`)
  - `Article`, `Comment`와 달리 `User`에 Auditing 필드가 없다. 의도적인 설계라면 주석으로 이유를 명시한다. 그렇지 않다면 `BaseEntity`를 상속하여 일관성을 유지한다.

- **`ArticleService.createArticle()` 태그 조회 N회 쿼리** (`domain/article/ArticleService.java:37-41`)
  - 태그 이름을 하나씩 조회하는 대신 `tagRepository.findByNameIn()`으로 일괄 조회 후 없는 태그만 신규 생성한다.

- **`CommentRestController` 테스트 누락**
  - `@WebMvcTest(CommentRestController.class)` 기반 테스트 클래스 추가가 필요하다. 현재 커버율 0%.

- **`ArticleSummaryProjection` N+1 잠재 위험** (`domain/article/ArticleRepository.java:107`)
  - `findProjectionsByAuthorId()`는 SpEL로 `author.profile.userName.nickname`에 접근하지만 JOIN FETCH가 없다. `@EntityGraph(attributePaths = {"author"})`를 추가하거나 DTO 프로젝션으로 전환한다.

- **`JpaConfig` 패키지 위치** (`infrastructure/repository/JpaConfig.java`)
  - `infrastructure/config/` 또는 `config/`로 이동하여 `build.gradle`의 JaCoCo 제외 패턴(`**/config/**`)과 일치시킨다.

### Low

- **`FollowRestController`와 `UserRestController`의 URL 루트 공유**
  - 기능이 명확히 분리되어 있으나 동일 URL 루트를 사용하므로, 팀 컨벤션에 따라 단일 컨트롤러로 통합하거나 분리 이유를 문서화한다.

- **`ArticleRestController.updateArticle()`이 `CreateArticleRequest` 재사용** (`application/article/ArticleRestController.java:82`)
  - 수정 요청 전용 `UpdateArticleRequest`로 분리하면 생성/수정 간 필드 차이를 명확히 표현할 수 있다.

- **`UserRepositoryTest.createdByIsSetOnSave()` 테스트 내용 불일치** (`test/domain/user/UserRepositoryTest.java:79-93`)
  - 테스트 이름과 실제 검증 내용이 다르다. `createdBy` 필드가 없는 `User` 대신 `Article`을 대상으로 검증하거나, 테스트 이름을 변경한다.

- **`Password` VO 평문 저장 명시** (`domain/user/Password.java`)
  - 인코딩 없이 평문이 저장됨을 주석으로 명시하여 학습 모듈의 범위를 명확히 한다.

---

## 6. 긍정적 패턴

- N+1 문제를 `findAll()`로 재현하고 `JOIN FETCH`, `@EntityGraph` 두 가지 해결 방식을 동일 리포지토리에서 비교하는 구조가 명확하다(`ArticleRepository.java`).
- `@SQLDelete` + `@SQLRestriction` 조합으로 소프트 삭제를 일관되게 적용하고, 리포지토리 테스트에서 native query로 `deleted = true` 여부까지 직접 검증한다.
- `@Version` 기반 낙관적 잠금을 `EntityManagerFactory`로 두 세션을 직접 생성하여 충돌 시나리오를 재현한 `UserRepositoryTest`는 시나리오 기반 테스트의 좋은 예다.
- `Page<T>` vs `Slice<T>` 차이를 `Hibernate Statistics`로 실제 쿼리 수까지 검증하는 방식이 개념 학습 목적에 적합하다.
- `GlobalExceptionHandler`가 RFC 9457 `ProblemDetail`을 사용하여 별도 에러 DTO 없이 표준 응답 구조를 제공한다.
- `DatabaseCleanup`이 엔티티 메타모델에서 테이블명을 동적으로 추출하고 `@ManyToMany` 조인 테이블을 수동 추가하는 방식으로 인수 테스트 격리를 구현한다.
- 3계층 테스트 전략(`@DataJpaTest`, `@WebMvcTest`, `@SpringBootTest` + RestAssured)이 모두 적용되어 있어 레이어별 검증 범위가 구분된다.
- `ArticleSpecification`이 null 조건을 `cb.conjunction()`으로 처리하여 `Specification.where()` 없이 바로 체이닝 가능한 구조다.
