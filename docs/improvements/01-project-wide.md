# 전체 프로젝트 차원 개선사항

## 개요

모든 모듈에 공통적으로 적용되어야 하는 개선사항을 정리합니다.

## 1. 테스트 인프라

### 1.1 JaCoCo 플러그인 추가

**우선순위**: P0

**현재 상태**: 모든 모듈에 JaCoCo 미적용, 커버리지 측정 불가

**적용 내용**:
- 각 모듈 `build.gradle.kts`에 `jacoco` 플러그인 추가
- 커버리지 최소 기준: 70%
- 제외 대상: `**/*Application*`, `**/config/**`, `**/dto/**`
- `test` 태스크 완료 후 `jacocoTestReport` 자동 실행 설정

**적용 모듈**: 전체

---

### 1.2 Testcontainers 표준화

**우선순위**: P1

**현재 상태**: PostgreSQL은 H2, MongoDB는 Embedded MongoDB 사용

**적용 내용**:
- 모듈별 베이스 통합 테스트 클래스 작성
  - `spring-data-jpa`: PostgreSQL 컨테이너
  - `spring-data-mongodb`: MongoDB 컨테이너
- `@DynamicPropertySource`로 datasource 동적 주입

**파일 위치**:
- `spring-data-jpa/src/test/java/io/github/js/integration/IntegrationTestBase.java`
- `spring-data-mongodb/src/test/java/io/github/js/integration/MongoIntegrationTestBase.java`

**적용 모듈**: spring-data-jpa, spring-data-mongodb

---

## 2. 예외 처리 표준화

### 2.1 GlobalExceptionHandler 구현

**우선순위**: P0

**현재 상태**: ControllerAdvice 없음, 에러 응답 형식 불통일

**적용 내용**:
- `ErrorResponse` 공통 DTO 정의: `code`, `message`, `timestamp`, `path`
- `@RestControllerAdvice`로 전역 예외 처리
- 처리 대상: 커스텀 `BusinessException`, `MethodArgumentNotValidException`, 일반 `Exception`

**파일 위치**: `{module}/src/main/java/io/github/js/infrastructure/exception/`

**적용 모듈**: 전체

---

### 2.2 Exception 계층 구조 정의

**우선순위**: P1

**적용 내용**:
- `BusinessException` 추상 베이스 클래스 정의 (`errorCode` 포함)
- 도메인별 예외 클래스 구현
  - `spring-data-jpa`: `UserNotFoundException`, `DuplicateEmailException`
  - `spring-security-jwt`: `JwtAuthenticationException`, `ExpiredJwtTokenException`, `InvalidJwtTokenException`
  - `spring-data-mongodb`: `ItemNotFoundException`, `DuplicateItemNameException`

**파일 위치**: `{module}/src/main/java/io/github/js/domain/{domain}/exception/`

**적용 모듈**: spring-data-jpa, spring-security-jwt, spring-data-mongodb

---

## 3. 환경 설정 관리

### 3.1 JWT Secret 환경변수화

**우선순위**: P0

**현재 상태**: JWT Secret이 코드에 하드코딩되어 있음 (보안 위험)

**적용 내용**:
- `application.yml`에 `${JWT_SECRET:fallback-value}` 형태로 변경
- `@ConfigurationProperties(prefix = "jwt")`로 설정 바인딩 (secret, expiration, issuer)
- `.env.example` 파일 추가
- `.gitignore`에 `.env` 추가

**파일 위치**:
- `spring-security-jwt/src/main/resources/application.yml`
- `spring-security-jwt/.env.example`

**적용 모듈**: spring-security-jwt

---

### 3.2 프로파일 분리

**우선순위**: P1

**적용 내용**:
- `application.yml` (공통), `application-local.yml`, `application-test.yml` 분리
- DB 접속 정보, JWT 설정 등 환경별 분리

**적용 모듈**: 전체

---

## 4. 문서화

### 4.1 루트 README.md 업데이트

**우선순위**: P0

**현재 상태**: 간단한 프로젝트 소개만 존재

**작성 내용**:
- 프로젝트 목적 및 모듈 구성표 (모듈명, 설명, 포트, 상태)
- 필수 요구사항 (Java 17, Docker)
- 환경 설정 및 실행 방법

**파일**: `/README.md`

---

### 4.2 모듈별 README 작성

**우선순위**: P0

**작성 내용**:
- 모듈 목적 및 주요 기능
- API 엔드포인트 목록
- 실행 방법 및 환경 설정
- 테스트 실행 방법

**파일 위치**: `{module}/README.md`

**적용 모듈**: 전체

---

## 5. 빌드 및 의존성 관리

### 5.1 공통 Gradle 설정 추출

**우선순위**: P1

**현재 상태**: Java toolchain, JUnit 플랫폼 설정이 각 모듈에 중복

**적용 내용**:
- `buildSrc`에 공통 컨벤션 플러그인 작성
- Java 17, JUnit 플랫폼, JaCoCo 공통 설정 추출

**파일**: `buildSrc/src/main/kotlin/common-conventions.gradle.kts`

---

### 5.2 Gradle Version Catalog 적용

**우선순위**: P2

**현재 상태**: 의존성 버전이 각 모듈에 분산

**적용 내용**:
- `gradle/libs.versions.toml` 생성
- testcontainers 등 공통 의존성 버전 중앙 관리

**파일**: `gradle/libs.versions.toml`

---

## 6. CI/CD 개선

### 6.1 GitHub Actions 워크플로우 확장

**우선순위**: P1

**현재 상태**: 테스트 실행만 수행, 커버리지 체크 없음

**적용 내용**:
- `test jacocoTestReport` 태스크 실행
- 테스트 결과 리포트 게시 (`publish-unit-test-result-action`)

**파일**: `.github/workflows/ci-test.yml`

---

## 7. 로깅 표준화

### 7.1 프로파일별 로그 레벨 설정

**우선순위**: P1

**적용 내용**:
- `logback-spring.xml`으로 프로파일별 로그 레벨 관리
- `local`: `io.github.js` DEBUG
- `test` / `prod`: `io.github.js` INFO

**파일 위치**: `{module}/src/main/resources/logback-spring.xml`

**적용 모듈**: 전체

---

## 권장 작업 순서

### Phase 1: 인프라 구축
1. JaCoCo 플러그인 추가
2. GlobalExceptionHandler 구현
3. JWT Secret 환경변수화
4. 루트 README 업데이트

### Phase 2: 표준화
5. 환경 설정 프로파일 분리
6. Exception 계층 구조 정의
7. 로깅 설정 통일

### Phase 3: 문서화
8. 모듈별 README 작성

### Phase 4: 고도화 (선택)
9. 공통 Gradle 설정 추출
10. CI/CD 커버리지 체크 확장
11. Gradle Version Catalog 적용

---

## 참고 문서

- [JaCoCo Gradle Plugin](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
- [Testcontainers](https://www.testcontainers.org/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)