# spring-security-jwt 모듈 개선안

## 모듈 개요

### 목적
Spring Security와 JWT를 활용한 인증/인가 시스템 학습

### 현재 구현 수준
- **JWT 생성/검증**: ✅ 기본 구현
- **Spring Security 설정**: ✅ FilterChain 구성
- **로그인 API**: ✅ 기본 구현
- **Refresh Token**: ❌ 미구현
- **Role 기반 접근 제어**: ⚠️ 부분 구현
- **테스트**: ⚠️ 부족 (추정 20-25%)

### 완성도
**50%** - 기본 인증 구현, Refresh Token 및 테스트 부족

## 현재 상태 분석

### 구현된 기능

#### 1. JWT 설정
**파일**: `spring-security-jwt/src/main/java/io/github/js/infrastructure/jwt/JWTConfiguration.java`

- JWT 생성/검증 로직 구현: 강점
- Secret Key 하드코딩 (보안 위험): 즉시 개선 필요

#### 2. Security Filter
**파일**: `spring-security-jwt/src/main/java/io/github/js/infrastructure/security/JwtAuthenticationFilter.java`

- JWT 토큰 추출 및 인증 객체 생성: 구현됨
- 예외 처리 미흡, 토큰 갱신 로직 없음

#### 3. Login API
**파일**: `spring-security-jwt/src/main/java/io/github/js/application/auth/AuthController.java`

- 기본 로그인 구현
- Refresh Token 미지원, 로그아웃 API 없음

### 누락된 기능
- Refresh Token 메커니즘
- 로그아웃 (토큰 무효화)
- 비밀번호 변경 API
- Role 기반 메서드 보안
- 토큰 갱신 API

---

## 개선 항목

### P0 (필수)

#### 1. JWT Secret 환경변수화

**파일**:
- `spring-security-jwt/src/main/resources/application.yml`
- `spring-security-jwt/.env.example`

적용 내용:
- `application.yml`에 `${JWT_SECRET:fallback-value}` 형태로 변경
- `@ConfigurationProperties(prefix = "jwt")`로 설정 클래스 분리 (secret, access-token-expiration, refresh-token-expiration, issuer)
- `.env.example` 파일 추가 (JWT_SECRET 키 안내)

---

#### 2. 통합 테스트 작성

**파일**:
- `spring-security-jwt/src/test/java/io/github/js/application/auth/AuthControllerTest.java`
- `spring-security-jwt/src/test/java/io/github/js/infrastructure/jwt/JwtTokenProviderTest.java`
- `spring-security-jwt/src/test/java/io/github/js/infrastructure/security/SecurityFilterChainTest.java`

**AuthControllerTest 케이스**:
- 로그인 성공 (Access Token 발급)
- 잘못된 비밀번호로 로그인 실패 (401)
- 존재하지 않는 사용자 로그인 실패 (401)
- 유효한 토큰으로 보호된 엔드포인트 접근 (200)
- 토큰 없이 보호된 엔드포인트 접근 (401)

**JwtTokenProviderTest 케이스**:
- Access Token 생성 성공
- 유효한 토큰 검증
- 잘못된 서명 토큰 검증 실패
- 사용자명 추출
- 역할 추출

**SecurityFilterChainTest 케이스**:
- Public 엔드포인트 인증 없이 접근 가능
- Protected 엔드포인트 인증 필요 (401)
- ADMIN 전용 엔드포인트에 USER 역할로 접근 (403)

---

#### 3. Exception 처리 강화

**파일**:
- `spring-security-jwt/src/main/java/io/github/js/infrastructure/jwt/exception/`
- `spring-security-jwt/src/main/java/io/github/js/infrastructure/security/JwtAuthenticationEntryPoint.java`
- `spring-security-jwt/src/main/java/io/github/js/infrastructure/security/JwtAccessDeniedHandler.java`

적용 내용:
- `JwtAuthenticationException`, `ExpiredJwtTokenException`, `InvalidJwtTokenException` 정의
- `AuthenticationEntryPoint`: 401 에러를 `ErrorResponse` 형식으로 응답
- `AccessDeniedHandler`: 403 에러를 `ErrorResponse` 형식으로 응답
- `SecurityConfig`에 두 핸들러 등록

---

### P1 (중요)

#### 4. Refresh Token 구현

**파일**:
- `spring-security-jwt/src/main/java/io/github/js/domain/token/RefreshToken.java`
- `spring-security-jwt/src/main/java/io/github/js/domain/token/RefreshTokenRepository.java`
- `spring-security-jwt/src/main/java/io/github/js/application/token/RefreshTokenService.java`

구현 내용:
- `RefreshToken` 엔티티: `token`, `username`, `expiryDate`, `createdAt`
- `isExpired()` 메서드
- `RefreshTokenService`: 생성, 검증, 삭제
- 로그인 응답에 `refreshToken` 추가
- `POST /api/auth/refresh` 엔드포인트: Refresh Token으로 새 Access Token 발급

---

#### 5. Role 기반 접근 제어 강화

**파일**: `spring-security-jwt/src/main/java/io/github/js/infrastructure/config/SecurityConfig.java`

적용 내용:
- `@EnableMethodSecurity(prePostEnabled = true)` 활성화
- Controller에 `@PreAuthorize` 적용
  - `hasRole('USER')`, `hasRole('ADMIN')`, `isAuthenticated()`
- 커스텀 보안 표현식 작성 (`@userSecurity.isOwner(...)`)

---

#### 6. 비밀번호 관리 API

적용 내용:
- `PUT /api/users/me/password`: 현재 비밀번호 확인 후 변경
- 요청 DTO: `currentPassword`, `newPassword` (최소 8자, 영문/숫자/특수문자 포함 검증)

---

### P2 (권장)

#### 7. 로그아웃 API

- `POST /api/auth/logout`: Refresh Token 삭제
- Access Token은 만료 시간까지 유효 (짧은 만료 시간으로 대응)

---

#### 8. 보안 감사 로그

**파일**: `spring-security-jwt/src/main/java/io/github/js/infrastructure/security/SecurityAuditListener.java`

- `AuthenticationSuccessEvent`: 로그인 성공 로그
- `AuthenticationFailureBadCredentialsEvent`: 로그인 실패 로그
- `LogoutSuccessEvent`: 로그아웃 성공 로그

---

## 변경 항목

### 1. JWT 검증 로직 개선

**현재 문제**: 예외 처리 불충분, 만료된 토큰과 잘못된 토큰 구분 없음

**개선 방향**: `ExpiredJwtException`, `SignatureException`, `MalformedJwtException` 등 예외별로 구분 처리

### 2. PasswordEncoder 설정 명확화

- BCryptPasswordEncoder strength 명시적 설정

---

## 신규 추가 항목

### 1. 인증 테스트 헬퍼

**파일**: `spring-security-jwt/src/test/java/io/github/js/support/AuthTestHelper.java`

- `generateTokenForUser(username)`: USER 역할 토큰 생성
- `generateTokenForAdmin(username)`: ADMIN 역할 토큰 생성

---

## 권장 작업 순서

1. JWT Secret 환경변수화 + `.env.example` 작성
2. `JwtTokenProviderTest`, `AuthControllerTest`, `SecurityFilterChainTest` 작성 → 커버리지 70%
3. 커스텀 JWT 예외 정의 + `AuthenticationEntryPoint`, `AccessDeniedHandler` 구현
4. `RefreshToken` Entity + `RefreshTokenService` + 토큰 갱신 API 구현
5. `@PreAuthorize` 적용 + 커스텀 표현식
6. (선택) 비밀번호 변경 API, 로그아웃 API, 보안 감사 로그

---

## 참고 문서

- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [JWT.io](https://jwt.io/)
- [JJWT Library](https://github.com/jwtk/jjwt)

### 관련 파일
- JWT Config: `spring-security-jwt/src/main/java/io/github/js/infrastructure/jwt/JWTConfiguration.java`
- Security Config: `spring-security-jwt/src/main/java/io/github/js/infrastructure/security/SecurityConfig.java`
- Auth Controller: `spring-security-jwt/src/main/java/io/github/js/application/auth/AuthController.java`

### 학습 포인트
1. **JWT 구조**: Header, Payload, Signature
2. **Refresh Token**: Access Token 재발급 메커니즘
3. **@PreAuthorize**: 메서드 수준 보안
4. **Filter Chain**: 요청 인증/인가 흐름
5. **Exception Handling**: 인증/인가 실패 처리