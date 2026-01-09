# Security Policy

## 지원 버전

현재 보안 업데이트가 제공되는 버전:

| Version | Supported          |
| ------- | ------------------ |
| 0.0.1-SNAPSHOT   | :white_check_mark: |

## 보안 취약점 보고

### 비공개 보고 (권장)

심각한 보안 취약점은 공개 Issue로 보고하지 마세요.

1. **GitHub Security Advisories** 사용
   - Repository → Security → Advisories → New draft security advisory
   - 취약점 상세 설명
   - 영향받는 버전 명시

2. **이메일 보고**
   - security@example.com (프로젝트 보안 담당자)
   - 제목: `[SECURITY] 취약점 제목`
   - 내용: 재현 방법, 영향 범위, 제안 수정

### 보고 시 포함 정보

- **취약점 유형**: SQL Injection, XSS, 인증 우회 등
- **영향받는 모듈**: spring-security-jwt, spring-data-jpa 등
- **재현 방법**: 단계별 상세 설명
- **영향 범위**: 데이터 유출, 권한 상승, DoS 등
- **제안 수정**: 패치 또는 완화 방법

### 응답 프로세스

1. **24시간 내**: 수신 확인
2. **48시간 내**: 초기 평가 및 심각도 분류
3. **7일 내**: 수정 계획 공유
4. **30일 내**: 패치 릴리즈 목표

## 심각도 분류

### Critical (긴급)
- 인증 없는 원격 코드 실행
- 대규모 데이터 유출
- 전체 시스템 손상

**대응**: 즉시 수정 및 긴급 패치 릴리즈

### High (높음)
- 인증 우회
- 권한 상승
- SQL Injection

**대응**: 7일 내 수정

### Medium (보통)
- XSS
- CSRF
- 정보 노출

**대응**: 30일 내 수정

### Low (낮음)
- 경로 노출
- 버전 정보 노출

**대응**: 다음 정기 릴리즈

## 보안 모범 사례

### 개발 시

1. **입력 검증**
   - 모든 사용자 입력 검증
   - Jakarta Validation 사용 (`@NotBlank`, `@Email` 등)

2. **인증/인가**
   - JWT 토큰 만료 시간 설정
   - Refresh token 구현
   - 권한 기반 접근 제어

3. **SQL Injection 방지**
   - JPA/Querydsl 사용 (파라미터 바인딩)
   - Native query 사용 금지

4. **XSS 방지**
   - 출력 시 이스케이프 처리
   - Content Security Policy 설정

5. **CSRF 방지**
   - Spring Security CSRF 토큰 활성화
   - Stateless API는 CSRF 비활성화 가능

6. **비밀 관리**
   - 환경 변수 사용
   - `.env` 파일 gitignore
   - AWS Secrets Manager / HashiCorp Vault 사용

7. **의존성 관리**
   - Dependabot 활성화
   - 주기적 업데이트
   - 취약점 스캔 (`./gradlew dependencyCheckAnalyze`)

### 배포 시

1. **HTTPS 강제**
   ```properties
   server.ssl.enabled=true
   security.require-ssl=true
   ```

2. **보안 헤더 설정**
   ```java
   http.headers()
       .xssProtection()
       .contentSecurityPolicy("default-src 'self'")
       .frameOptions().deny();
   ```

3. **Rate Limiting**
   - Spring Cloud Gateway
   - Bucket4j

4. **로깅**
   - 민감 정보 마스킹
   - 실패한 로그인 시도 로깅

## 취약점 공개 정책

### 수정 후 공개

- 패치 릴리즈 후 7일 뒤 공개
- Security Advisory 게시
- CVE 등록 (해당 시)

### 공개 내용

- 취약점 설명
- 영향받는 버전
- 수정된 버전
- 완화 방법
- 발견자 크레딧

## 의존성 보안

### 자동 업데이트

Dependabot 설정 (`.github/dependabot.yml`):
- Gradle 의존성 주간 체크
- 보안 업데이트 우선

### 수동 체크

```bash
# 취약점 스캔
./gradlew dependencyCheckAnalyze

# 업데이트 가능 확인
./gradlew dependencyUpdates
```

## 과거 보안 공지

현재 공개된 보안 취약점 없음.

## 연락처

- **보안 담당자**: security@example.com
- **GitHub**: @jaesung-ahn
- **비상 연락**: [비공개]

---

**보안 연구자 여러분께 감사드립니다.** 🔒
