# Specification vs QueryDSL

Spring Data JPA에서 동적 쿼리를 표현하는 두 가지 방식의 비교.

---

## 구조 비교

| 항목 | Specification | QueryDSL |
|---|---|---|
| 기반 기술 | JPA Criteria API 래퍼 | 코드 생성(Q타입) |
| 타입 안전 | `root.get("author")` — 문자열 | `QArticle.article.author` — 컴파일 타임 |
| null 조건 처리 | `cb.conjunction()` 명시 필요 | `where(null, ...)` 자동 무시 |
| 조건 조합 | `.and()` / `.or()` 체이닝 | `where(expr1, expr2, ...)` 가변인자 |
| 서브쿼리 | `Subquery<T>` — 장황 | `JPAExpressions.selectOne()` — 간결 |
| 동적 정렬 | 별도 `Pageable` 구성 필요 | `OrderSpecifier<?>` 직접 조합 |
| 집계/JOIN Projection | 표현 어려움 | `Projections.constructor()` 직접 지정 |
| Spring Data 통합 | `JpaSpecificationExecutor` 상속만으로 사용 | `JPAQueryFactory` 빈 + 커스텀 구현체 필요 |
| Q타입 생성 | 불필요 | 빌드 시 자동 생성 (`annotationProcessor`) |

---

## Specification이 유리한 경우

- 단순 AND/OR 조건 조합으로 충분한 동적 검색
- 추가 의존성 없이 Spring Data JPA만으로 해결하고 싶을 때
- 조건 객체를 별도 클래스로 분리해 재사용할 때

```java
Specification<Article> spec = ArticleSpecification.hasAuthorName(authorName)
        .and(ArticleSpecification.hasTag(tag))
        .and(ArticleSpecification.titleContains(keyword));
articleRepository.findAll(spec, pageable);
```

---

## QueryDSL이 유리한 경우

- 타입 안전한 경로 표현이 필요할 때 (오타 컴파일 타임 감지)
- EXISTS 서브쿼리, 동적 정렬, 집계 프로젝션 등 복잡한 쿼리
- null 조건을 간결하게 처리하고 싶을 때

```java
// null인 조건은 where()에서 자동 무시
queryFactory.selectFrom(article)
        .where(hasAuthorName(authorName), hasTag(tag), titleContains(keyword))
        .fetch();
```

---

## 이 프로젝트 구현 패턴 인덱스

### Specification

| 패턴 | 위치 |
|---|---|
| 동적 필터링 (`hasAuthorName`, `hasTag`, `titleContains`, `createdAfter`) | `ArticleSpecification` |
| Specification 조합 + 페이지네이션 | `ArticleService.searchArticles()` |

### QueryDSL

| 패턴 | 위치 |
|---|---|
| IN + DISTINCT (태그 OR 조건) | `ArticleRepositoryImpl.findByAnyTagName()` |
| BooleanExpression 동적 검색 + `PageableExecutionUtils` count 최적화 | `ArticleRepositoryImpl.searchArticles()` |
| EXISTS 서브쿼리 (`JPAExpressions.selectOne()`) | `ArticleRepositoryImpl.findArticlesHavingComment()` |
| 동적 정렬 (`OrderSpecifier<?>`) | `ArticleRepositoryImpl.findWithDynamicSort()` |
| exists 최적화 (`selectOne().fetchFirst() != null`) | `ArticleRepositoryImpl.existsArticleByAuthorId()` |
| `Projections.constructor()` + 명시적 JOIN alias | `ArticleRepositoryImpl.searchArticles()` |

---

## PathInits 주의사항

QueryDSL의 Q타입은 기본적으로 `PathInits.DIRECT2`로 생성된다.
`QArticle.article.author.profile.userName`처럼 depth 3 이상의 경로는 `null`이 된다.

```java
// 잘못된 방식 — article 기준 depth 3이므로 userName이 null
article.author.profile.userName.nickname

// 올바른 방식 — QUser 기준 depth 2로 초기화
QUser author = new QUser("author");
queryFactory.from(article).innerJoin(article.author, author);
author.profile.userName.nickname  // depth 2, 정상 접근
```
