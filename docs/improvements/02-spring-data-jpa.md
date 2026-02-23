# spring-data-jpa 모듈 개선안

## 모듈 개요

### 목적
JPA 및 Spring Data JPA를 활용한 영속성 레이어 학습

### 현재 구현 수준
- **기본 CRUD**: ✅ 완성
- **Value Objects**: ✅ Email, Name 등 활용
- **Repository**: ✅ JpaRepository 사용
- **Service 레이어**: ✅ 기본 구현
- **테스트**: ⚠️ 매우 부족 (추정 25-30%)

### 완성도
**60%** - 기본 구조 완성, 테스트 및 고급 기능 부족

## 현재 상태 분석

### 구현된 기능

#### 1. Entity 설계
**파일**: `spring-data-jpa/src/main/java/io/github/js/domain/user/User.java`

- Value Objects (Email, Name) 활용: 강점
- Auditing 미적용 (createdAt, updatedAt): 개선 필요

#### 2. Repository
**파일**: `spring-data-jpa/src/main/java/io/github/js/domain/user/UserRepository.java`

- JpaRepository 기본 사용
- 커스텀 쿼리 메서드 없음, Querydsl 미적용, 페이징/정렬 예제 없음

#### 3. Service / Controller
**파일**:
- `spring-data-jpa/src/main/java/io/github/js/application/user/UserService.java`
- `spring-data-jpa/src/main/java/io/github/js/application/user/UserRestController.java`

- 기본 CRUD 구현됨
- 예외 처리 부족, 입력 검증 미흡

### 누락된 기능
- Querydsl 설정 및 활용
- Auditing (BaseEntity)
- 페이징/정렬 API
- 복잡한 쿼리 예제
- 트랜잭션 전파 예제

---

## 개선 항목

### P0 (필수)

#### 1. 테스트 커버리지 70% 달성

**1.1 UserServiceTest 작성**

**파일**: `spring-data-jpa/src/test/java/io/github/js/application/user/UserServiceTest.java`

테스트 케이스:
- 사용자 생성 성공
- 중복 이메일로 생성 실패 (`DuplicateEmailException`)
- 사용자 조회 성공
- 존재하지 않는 사용자 조회 실패 (`UserNotFoundException`)
- 사용자 수정 (정상/실패)
- 사용자 삭제 (정상/실패)

---

**1.2 UserRestControllerTest 작성**

**파일**: `spring-data-jpa/src/test/java/io/github/js/application/user/UserRestControllerTest.java`

테스트 케이스:
- `POST /api/users` 성공 (201 Created, Location 헤더 포함)
- `POST /api/users` 잘못된 이메일 형식 (400)
- `GET /api/users/{id}` 성공 (200)
- `GET /api/users/{id}` 존재하지 않음 (404)
- `PUT /api/users/{id}` 수정 성공
- `DELETE /api/users/{id}` 삭제 성공

---

**1.3 UserRepositoryTest 보완**

**파일**: `spring-data-jpa/src/test/java/io/github/js/domain/user/UserRepositoryTest.java`

추가 테스트:
- `findByEmail` 성공
- `existsByEmail` 검증
- `findAllByNameContaining` 목록 조회

---

#### 2. Exception 처리 강화

**파일**: `spring-data-jpa/src/main/java/io/github/js/domain/user/exception/`

- `UserNotFoundException`: errorCode `USER_NOT_FOUND`
- `DuplicateEmailException`: errorCode `DUPLICATE_EMAIL`
- Service 레이어에서 예외 발생 처리 추가

---

#### 3. 입력 검증 추가

**파일**: `spring-data-jpa/src/main/java/io/github/js/application/user/dto/CreateUserRequest.java`

- `email`: `@NotBlank`, `@Email`
- `name`: `@NotBlank`, `@Size(min=2, max=50)`
- Controller에 `@Valid` 적용

---

### P1 (중요)

#### 4. Querydsl 설정 및 활용

**파일**:
- `spring-data-jpa/src/main/java/io/github/js/domain/user/UserRepositoryCustom.java`
- `spring-data-jpa/src/main/java/io/github/js/domain/user/UserRepositoryImpl.java`
- `spring-data-jpa/src/main/java/io/github/js/infrastructure/config/QuerydslConfig.java`

구현 내용:
- `UserRepositoryCustom`: `searchUsers(condition)`, `searchUsersWithPaging(condition, pageable)`
- `UserRepositoryImpl`: `JPAQueryFactory` 기반 email, name 동적 쿼리
- `JPAQueryFactory` 빈 설정

---

#### 5. Auditing 설정

**파일**:
- `spring-data-jpa/src/main/java/io/github/js/domain/common/BaseEntity.java`
- `spring-data-jpa/src/main/java/io/github/js/infrastructure/config/JpaAuditingConfig.java`

구현 내용:
- `BaseEntity`: `@CreatedDate`, `@LastModifiedDate` 필드 포함
- `@EnableJpaAuditing` 설정
- `User` 엔티티에 `BaseEntity` 적용

---

#### 6. 페이징/정렬 API

- `GET /api/users` 엔드포인트에 `Pageable` 파라미터 추가
- `@PageableDefault(size=20, sort="createdAt", direction=DESC)` 적용
- `UserSearchCondition` DTO로 검색 조건 수신

---

### P2 (권장)

#### 7. JPA 학습 예제 추가

학습 목적의 예제 작성:
- **N+1 문제**: Fetch Join, EntityGraph 비교
- **트랜잭션 전파**: `REQUIRES_NEW` 활용 시나리오 (예: 알림 서비스)
- **변경 감지 (Dirty Checking)**: 명시적 save 없이 수정
- **통계 쿼리**: Querydsl Projections 활용

---

## 변경 항목

### 1. Value Object 불변성 확인
- `Email`, `Name` 필드가 실질적으로 불변인지 검토
- `equals`/`hashCode` 구현 일관성 확인

### 2. Entity ID 생성 전략
- 현재 `GenerationType.IDENTITY` → PostgreSQL 환경에서 `SEQUENCE` 방식 검토
- 이유: IDENTITY는 배치 insert 최적화 불가

---

## 신규 추가 항목

### 1. 통합 테스트 환경

**파일**: `spring-data-jpa/src/test/java/io/github/js/integration/IntegrationTestBase.java`

- `@Testcontainers` + PostgreSQL 컨테이너 설정
- `@DynamicPropertySource`로 datasource 주입

### 2. 데이터 초기화 스크립트

**파일**: `spring-data-jpa/src/main/resources/data.sql`

- `application-local.yml`에서 `spring.sql.init.mode=always` 설정
- 개발 환경 샘플 데이터 삽입

---

## 권장 작업 순서

1. JaCoCo 추가 + IntegrationTestBase 작성
2. `UserServiceTest`, `UserRestControllerTest`, `UserRepositoryTest` 작성 → 커버리지 70%
3. 커스텀 예외 정의 + Service 예외 처리 + DTO 검증
4. Querydsl 의존성 추가 + Custom Repository + 검색 API
5. `BaseEntity` + Auditing 설정
6. (선택) JPA 학습 예제 추가

---

## 참고 문서

- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Querydsl Reference](http://querydsl.com/static/querydsl/latest/reference/html/)

### 관련 파일
- Entity: `spring-data-jpa/src/main/java/io/github/js/domain/user/User.java`
- Repository: `spring-data-jpa/src/main/java/io/github/js/domain/user/UserRepository.java`
- Service: `spring-data-jpa/src/main/java/io/github/js/application/user/UserService.java`
- Controller: `spring-data-jpa/src/main/java/io/github/js/application/user/UserRestController.java`

### 학습 포인트
1. **Value Objects**: 도메인 개념을 타입으로 표현
2. **Querydsl**: 타입 안전한 동적 쿼리
3. **Auditing**: 공통 메타데이터 자동 관리
4. **N+1 문제**: Fetch Join, EntityGraph로 해결
5. **트랜잭션 전파**: 비즈니스 요구사항에 따른 전략 선택