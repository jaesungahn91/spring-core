# Spring Core 프로젝트 개선 로드맵

## 개요

본 문서는 Spring Core 프로젝트의 개선 작업을 Phase별로 정리한 로드맵입니다.

## 전체 일정 요약

| Phase | 목표 | 주요 산출물 |
|-------|------|------------|
| Phase 1 | 인프라 및 품질 기반 구축 | JaCoCo, ExceptionHandler, 환경변수화, README |
| Phase 2 | 테스트 커버리지 70% 달성 | 모듈별 테스트 코드, 통합 테스트 |
| Phase 3 | 기능 완성도 향상 | Querydsl, Refresh Token, MongoDB REST API, 배치 Job |
| Phase 4 | 문서화 및 고급 기능 | 모듈별 README, 아키텍처 문서, API 문서 통합 |

---

## Phase 1: 인프라 및 품질 기반 구축

### 목표
모든 모듈에 공통 적용되는 기반 인프라 구축 및 보안 취약점 해결

### 우선순위
**Critical** - 다른 모든 작업의 전제 조건

### 작업 항목

#### 1.1 JaCoCo 플러그인 추가
- 모든 모듈 `build.gradle.kts`에 JaCoCo 추가
- 커버리지 최소 기준 70% 설정
- CI 워크플로우 업데이트

#### 1.2 JWT Secret 환경변수화
- `application.yml` 환경변수 참조 형태로 변경
- `JwtProperties` 설정 클래스 작성
- `.env.example` 파일 추가

#### 1.3 GlobalExceptionHandler 구현
- `ErrorResponse` DTO 정의
- 모든 모듈에 `@RestControllerAdvice` 적용
- 도메인별 커스텀 예외 클래스 정의

#### 1.4 환경 설정 프로파일 분리
- `application-local.yml`, `application-test.yml` 분리
- 민감 정보 환경변수화

#### 1.5 루트 및 모듈별 README 작성
- `/README.md`: 프로젝트 개요, 모듈 구성, 실행 방법
- `{module}/README.md`: 모듈 목적, API 목록, 실행 방법

#### 1.6 Testcontainers 통합 테스트 환경 구축
- `spring-data-jpa`: PostgreSQL 컨테이너
- `spring-data-mongodb`: MongoDB 컨테이너

### Phase 1 완료 기준
- [ ] 모든 모듈에 JaCoCo 적용 및 리포트 생성 가능
- [ ] JWT Secret 환경변수로 관리
- [ ] GlobalExceptionHandler 모든 모듈 적용
- [ ] 환경별 설정 파일 분리 완료
- [ ] README 작성 완료
- [ ] Testcontainers 통합 테스트 환경 동작

---

## Phase 2: 테스트 커버리지 70% 달성

### 목표
모든 모듈의 테스트 커버리지를 최소 70% 이상으로 확보

### 우선순위
**High** - 코드 품질 및 리팩터링 신뢰도 확보

### 작업 항목

#### spring-data-jpa
- `UserServiceTest`: CRUD 성공/실패, 예외 발생 케이스
- `UserRestControllerTest`: 전체 API 엔드포인트
- `UserRepositoryTest`: 커스텀 쿼리 메서드

#### spring-security-jwt
- `JwtTokenProviderTest`: 생성, 검증, 정보 추출
- `AuthControllerTest`: 로그인, 인증된 엔드포인트 접근
- `SecurityFilterChainTest`: Public/Protected 엔드포인트, 역할 기반 접근

#### spring-data-mongodb
- `ItemServiceTest`: CRUD 성공/실패, 예외 발생 케이스
- `ItemRestControllerTest`: 전체 API 엔드포인트
- `ItemRepositoryTest`: 기본 CRUD

#### spring-rest-docs
- `OrderControllerTest`: 전체 CRUD API 문서화 포함

#### spring-batch
- `UserImportJobTest`: Job/Step 실행, 필터링 검증
- `FileCleanupJobTest`: Tasklet 실행

### Phase 2 완료 기준
- [ ] 모든 모듈 커버리지 70% 이상
- [ ] CI에서 커버리지 체크 통과
- [ ] 모든 테스트 통과

---

## Phase 3: 기능 완성도 향상

### 목표
각 모듈의 핵심 기능 완성 및 학습 목적 달성

### 우선순위
**Medium** - 학습 목적 달성 및 실무 수준 완성도

### 작업 항목

#### spring-data-jpa
- Querydsl 설정 + Custom Repository + 검색 API
- `BaseEntity` + Auditing 적용
- 페이징/정렬 API

#### spring-security-jwt
- Refresh Token 구현 (Entity, Service, 갱신 API)
- `@PreAuthorize` 메서드 보안 적용
- 비밀번호 변경 API

#### spring-data-mongodb
- `ItemService`, `ItemRestController` 구현
- 인덱스 설정 + Auditing
- Custom Repository + 검색 API

#### spring-batch
- Chunk 기반 Job (CSV → DB)
- Tasklet 기반 Job (파일 정리)
- 스케줄링 설정

### Phase 3 완료 기준
- [ ] spring-data-jpa: Querydsl, Auditing, 페이징 완료
- [ ] spring-security-jwt: Refresh Token, @PreAuthorize 완료
- [ ] spring-data-mongodb: REST API, Custom Repository 완료
- [ ] spring-batch: 2개 Job 구현 완료
- [ ] 모든 기능 테스트 통과
- [ ] 커버리지 70% 유지

---

## Phase 4: 문서화 및 고급 기능

### 목표
API 문서화 완성 및 선택적 고급 기능 추가

### 우선순위
**Low** - 완성도 향상

### 작업 항목

#### 문서화
- `spring-rest-docs`: `index.adoc` 완성, 에러 응답/Constraints 문서화
- 아키텍처 문서: `docs/architecture/` 디렉토리 구성
  - 모듈 구조 설명
  - 데이터 흐름도
  - 설계 의도

#### 고급 기능 (선택)
- N+1 문제 해결 예제 (JPA)
- 트랜잭션 전파 예제 (JPA)
- 텍스트 검색 (MongoDB)
- 집계 파이프라인 (MongoDB)
- 배치 모니터링 API (Batch)

### Phase 4 완료 기준
- [ ] spring-rest-docs 전체 API 문서화 완료
- [ ] 아키텍처 문서 작성 완료
- [ ] 선택 고급 기능 1개 이상 구현

---

## 마일스톤

| 마일스톤 | 완료 조건 |
|----------|----------|
| M1: 기반 구축 | JaCoCo, ExceptionHandler, 환경변수, README 완료, CI 동작 |
| M2: 테스트 완성 | 모든 모듈 커버리지 70%, 통합 테스트 환경 구축 |
| M3: 기능 완성 | 모든 모듈 핵심 기능 구현, 학습 목적 달성 |
| M4: 문서화 완료 | API 문서, 아키텍처 문서 완성 |

---

## 진행 상황 추적

### 체크 방법
- 각 Phase 완료 기준 체크리스트 점검
- JaCoCo 커버리지 리포트 확인
- CI 빌드 상태 확인

---

**작성일**: 2026-01-27