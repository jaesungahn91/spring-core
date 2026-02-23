# spring-rest-docs 모듈 개선안

## 모듈 개요

### 목적
Spring REST Docs를 활용한 API 문서 자동화 학습

### 현재 구현 수준
- **REST Docs 설정**: ✅ 완료
- **CRUD API**: ⚠️ 일부만 구현
- **API 문서화**: ⚠️ 구현된 API만 문서화
- **에러 응답 문서화**: ❌ 없음
- **테스트**: ⚠️ 부족 (추정 40-50%)

### 완성도
**55%** - REST Docs 설정 완료, CRUD 전체 구현 및 문서화 필요

## 현재 상태 분석

### 구현된 기능

#### 1. REST Docs 설정
**파일**: `spring-rest-docs/build.gradle.kts`

**강점**:
- asciidoctor 플러그인 설정
- snippets 디렉토리 설정
- 문서 생성 자동화

**개선 필요**:
- 문서 템플릿 커스터마이징 없음

#### 2. Order API (일부)
**파일**: `spring-rest-docs/src/main/java/io/github/js/application/order/`

**강점**:
- 기본 엔티티 정의
- Repository 구현

**개선 필요**:
- CRUD 일부만 구현
- 복잡한 비즈니스 로직 없음

#### 3. 테스트 및 문서화
**파일**: `spring-rest-docs/src/test/java/io/github/js/application/order/OrderControllerTest.java`

**강점**:
- REST Docs 적용

**개선 필요**:
- 전체 API 문서화 필요
- 에러 케이스 문서화 없음
- Constraints 문서화 없음

### 누락된 기능
- 전체 CRUD API 구현
- 페이징/정렬 API
- 검색 API
- 에러 응답 문서화
- 인증 관련 문서화
- Constraints 문서화

---

## 개선 항목

### P0 (필수)

#### 1. 전체 CRUD API 구현 및 문서화

전체 CRUD API를 구현하고 각 엔드포인트를 REST Docs로 문서화합니다.

**작업 내용**:
- OrderService 완성 (생성, 조회, 수정, 삭제, 취소)
- OrderRestController 완성
- DTO 정의 (CreateOrderRequest, UpdateOrderRequest, OrderResponse)
- 입력 검증 추가

**파일 위치**:
- `spring-rest-docs/src/main/java/io/github/js/application/order/OrderService.java`
- `spring-rest-docs/src/main/java/io/github/js/application/order/OrderRestController.java`
- `spring-rest-docs/src/main/java/io/github/js/application/order/dto/`

---

#### 2. 전체 API 테스트 및 문서화

**작업 내용**:

각 API 엔드포인트에 대해 테스트를 작성하고 REST Docs 문서화:

1. **주문 생성 (POST /api/orders)**
   - 요청/응답 필드 문서화
   - 성공 케이스 (201 Created)

2. **주문 조회 (GET /api/orders/{id})**
   - Path Parameter 문서화
   - 성공 케이스 (200 OK)

3. **주문 목록 조회 (GET /api/orders)**
   - Query Parameter 문서화 (page, size, sort)
   - 페이징 응답 구조 문서화

4. **주문 수정 (PUT /api/orders/{id})**
   - 요청 필드 문서화
   - 성공 케이스 (204 No Content)

5. **주문 삭제 (DELETE /api/orders/{id})**
   - Path Parameter 문서화
   - 성공 케이스 (204 No Content)

6. **주문 취소 (POST /api/orders/{id}/cancel)**
   - 비즈니스 로직 문서화
   - 성공 케이스 (204 No Content)

**파일**: `spring-rest-docs/src/test/java/io/github/js/application/order/OrderControllerTest.java`

---

### P1 (중요)

#### 3. 에러 응답 문서화

**작업 내용**:

공통 에러 응답 형식을 문서화합니다:

1. **404 Not Found**: 리소스가 존재하지 않을 때
2. **400 Bad Request**: 입력 검증 실패 시
3. **500 Internal Server Error**: 서버 오류 시

**파일**: `spring-rest-docs/src/test/java/io/github/js/common/ErrorResponseTest.java`

**문서화 항목**:
- `code`: 에러 코드
- `message`: 에러 메시지
- `timestamp`: 에러 발생 시각
- `path`: 요청 경로

---

#### 4. Constraints 문서화

**작업 내용**:

DTO 필드의 검증 제약 조건을 문서화합니다.

**방법**:
1. `ConstrainedFields` 유틸리티 클래스 작성
2. `ConstraintDescriptions` 활용
3. 각 필드의 제약 조건을 문서에 포함

**예시**:
- `customerId`: NotNull
- `items`: NotEmpty, 최소 1개 이상
- `quantity`: Min(1)
- `price`: Min(0)

**파일**: `spring-rest-docs/src/test/java/io/github/js/support/ConstrainedFields.java`

---

#### 5. 인증 헤더 문서화

**작업 내용**:

`OrderControllerTest`에 인증 헤더 문서화를 추가합니다.

**항목**:
- `Authorization` 헤더: Bearer 토큰 형식
- 인증 실패 시 401 Unauthorized 응답

---

### P2 (권장)

#### 6. 커스텀 Snippet 템플릿

**작업 내용**:

REST Docs 기본 템플릿을 커스터마이징하여 문서 가독성을 향상시킵니다.

**파일**: `spring-rest-docs/src/test/resources/org/springframework/restdocs/templates/`

---

#### 7. API 문서 HTML 정적 서빙

**작업 내용**:

생성된 HTML 문서를 애플리케이션에서 직접 서빙합니다.

**설정**:
1. WebConfig에서 정적 리소스 핸들러 추가
2. build.gradle.kts 수정 (bootJar에 문서 포함)
3. `/docs` 경로로 접근 가능하도록 설정

**접근 경로**: `http://localhost:8080/docs/index.html`

---

#### 8. API 문서 커스터마이징

**작업 내용**:

AsciiDoc `index.adoc` 파일을 작성하여 문서 구조를 체계화합니다.

**구성**:
1. **개요**: API 소개
2. **HTTP 동사**: GET, POST, PUT, DELETE 설명
3. **HTTP 상태 코드**: 각 코드의 의미
4. **에러 응답**: 공통 에러 형식
5. **인증**: Bearer 토큰 사용법
6. **리소스**: 각 API 엔드포인트 상세 설명

**파일**: `spring-rest-docs/src/docs/asciidoc/index.adoc`

---

## 변경 항목

### 1. RestDocsMockMvc 설정 개선

**현재 문제**:
- 기본 설정만 사용

**개선 방안**:

요청/응답 전처리를 통해 문서 가독성 향상:
- URI를 프로덕션 환경으로 변경 (https://api.example.com)
- Pretty Print 적용
- 포트 번호 제거

**파일**: `spring-rest-docs/src/test/java/io/github/js/config/RestDocsConfiguration.java`

---

### 2. 문서 버전 관리

**작업 내용**:

API 문서에 버전 정보를 명시합니다.

**파일**: `spring-rest-docs/src/docs/asciidoc/index.adoc`

---

## 권장 작업 순서

### Step 1: CRUD API 완성
1. OrderService 완성
2. OrderRestController 완성
3. DTO 정의

### Step 2: 전체 API 문서화
4. OrderControllerTest 완성
5. 모든 엔드포인트 테스트 작성
6. REST Docs 적용

### Step 3: 에러 및 제약조건 문서화
7. ErrorResponseTest 작성
8. ConstrainedFields 적용

### Step 4: 문서 커스터마이징
9. index.adoc 작성
10. 커스텀 Snippet 템플릿
11. HTML 정적 서빙 설정

### Step 5: 추가 기능 (선택)
12. 인증 API 문서화
13. Postman Collection 생성

---

## 참고 문서

### 공식 문서
- [Spring REST Docs](https://docs.spring.io/spring-restdocs/docs/current/reference/htmlsingle/)
- [AsciiDoc Syntax](https://docs.asciidoctor.org/asciidoc/latest/syntax-quick-reference/)

### 관련 파일
- Test: `spring-rest-docs/src/test/java/io/github/js/application/order/OrderControllerTest.java`
- Docs: `spring-rest-docs/src/docs/asciidoc/index.adoc`
- Build: `spring-rest-docs/build.gradle.kts`

### 학습 포인트
1. **테스트 기반 문서화**: 테스트 코드가 문서 소스
2. **Snippet**: 테스트 결과를 문서 조각으로 생성
3. **AsciiDoc**: 문서 작성 마크업 언어
4. **자동화**: 빌드 시 자동 문서 생성
5. **정확성**: 코드와 문서 불일치 방지