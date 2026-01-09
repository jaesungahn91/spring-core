# Git Hooks

프로젝트 전용 Git hooks 스크립트입니다.

## 개요

Git hooks는 특정 Git 이벤트 발생 시 자동으로 실행되는 스크립트입니다. 커밋 전 코드 품질 검증, 커밋 메시지 포맷 검증 등을 자동화합니다.

## 설치

```bash
# .githooks 디렉토리를 Git hooks로 설정
./githooks/install-hooks.sh
```

또는 수동 설치:
```bash
chmod +x .githooks/*
git config core.hooksPath .githooks
```

## 현재 활성화된 Hooks

| Hook | 실행 시점 | 목적 |
|------|----------|------|
| `pre-commit` | 커밋 전 | 코드 품질 검증 |
| `commit-msg` | 커밋 메시지 작성 후 | 메시지 포맷 검증 |

## pre-commit

커밋 전 자동으로 실행되는 검증 스크립트입니다.

### 검증 항목

1. **Merge Conflict Markers**
   ```
   <<<<<<< HEAD
   =======
   >>>>>>> branch
   ```
   - 충돌 마커 발견 시 커밋 차단
   - 충돌 해결 후 다시 시도

2. **TODO/FIXME Comments**
   ```java
   // TODO: implement this
   // FIXME: bug here
   ```
   - 발견 시 경고
   - 계속 진행 여부 확인

3. **Debugging Statements**
   ```java
   System.out.println("debug");
   e.printStackTrace();
   ```
   - 발견 시 경고
   - 의도적인 경우 계속 진행 가능

4. **Large Files (> 1MB)**
   - 대용량 파일 커밋 시 경고
   - 확인 후 진행

5. **Potential Secrets**
   ```properties
   api.key=sk_live_abc123...
   password=secret123
   ```
   - 민감한 정보 패턴 감지 시 차단
   - 환경 변수 사용 권장

### 실행 결과

```bash
# 성공
Running pre-commit checks...
Pre-commit checks passed

# 실패
Running pre-commit checks...
Potential secrets detected in staged files:
src/main/resources/application.properties:3:api.key="sk_live_..."
Please remove secrets and use environment variables.
```

## commit-msg

커밋 메시지 형식을 검증합니다.

### 규칙

커밋 메시지는 다음 형식을 따라야 합니다:

```
<type>: <subject>

[optional body]

[optional footer]
```

### 허용된 Types

- `feat`: 새 기능
- `fix`: 버그 수정
- `docs`: 문서 변경
- `style`: 코드 포맷팅 (로직 변경 없음)
- `refactor`: 리팩터링
- `test`: 테스트 추가/수정
- `chore`: 빌드, 설정 변경
- `perf`: 성능 개선
- `ci`: CI 설정 변경
- `revert`: 커밋 되돌리기

### 예제

```bash
# ✅ 올바른 형식
feat: JWT 토큰 갱신 기능 추가

# ✅ 본문 포함
fix: 유저 조회 시 NPE 발생 수정

UserRepository.findById()가 empty를 반환할 때
.get() 호출로 인한 NPE 수정

# ❌ 잘못된 형식
Add feature  # type 없음
feat add feature  # 콜론 없음
feature: add new feature  # 잘못된 type
FEAT: add feature  # 대문자 사용
```

## Hooks 비활성화

### 일시적 비활성화 (1회)

```bash
# --no-verify 플래그 사용
git commit --no-verify -m "feat: emergency fix"
```

**주의:** 긴급 상황에서만 사용하세요.

### 완전 비활성화

```bash
# hooks 설정 제거
git config --unset core.hooksPath

# 다시 활성화
git config core.hooksPath .githooks
```

## 커스텀 Hook 추가

### 1. Hook 스크립트 생성

```bash
cat > .githooks/pre-push << 'EOF'
#!/bin/bash

echo "Running pre-push checks..."

# 예: 테스트 실행
./gradlew test

if [ $? -ne 0 ]; then
  echo "Tests failed. Push aborted."
  exit 1
fi

echo "Pre-push checks passed"
exit 0
EOF
```

### 2. 실행 권한 부여

```bash
chmod +x .githooks/pre-push
```

### 3. 설치 스크립트 업데이트

`install-hooks.sh`에 추가:
```bash
chmod +x .githooks/pre-push
echo "  - pre-push: Runs tests before push"
```

## 사용 가능한 Git Hooks

| Hook | 실행 시점 | 용도 예시 |
|------|----------|-----------|
| `pre-commit` | 커밋 전 | 코드 린팅, 포맷팅 |
| `prepare-commit-msg` | 커밋 메시지 준비 | 메시지 템플릿 삽입 |
| `commit-msg` | 커밋 메시지 작성 후 | 메시지 검증 |
| `post-commit` | 커밋 완료 후 | 알림, 로깅 |
| `pre-push` | 푸시 전 | 테스트 실행 |
| `pre-rebase` | 리베이스 전 | 안전 검증 |

## 트러블슈팅

### Hook이 실행되지 않음

```bash
# 1. 실행 권한 확인
ls -l .githooks/

# 2. 권한 부여
chmod +x .githooks/*

# 3. Git 설정 확인
git config core.hooksPath
# 출력: .githooks

# 4. 설정이 없다면
git config core.hooksPath .githooks
```

### Hook이 너무 느림

```bash
# 특정 검증 비활성화 (스크립트 수정)
# 예: 대용량 파일 체크 주석 처리

# 또는 일시적으로 스킵
git commit --no-verify
```

### 잘못된 경고

```bash
# Hook 스크립트 수정
vim .githooks/pre-commit

# 특정 패턴 제외
# 예: test 파일에서는 System.out.println 허용
```

## 모범 사례

### DO
- **빠른 실행** (< 5초)
- **명확한 에러 메시지**
- **우회 가능** (--no-verify)
- **팀과 공유** (저장소에 포함)

### DON'T
- 긴 작업 (전체 테스트 스위트)
- 외부 의존성 (네트워크 요청)
- 모호한 실패 원인
- 강제 적용 (비상시 우회 불가)

## Hook 작성 팁

### 1. 종료 코드 사용

```bash
# 성공
exit 0

# 실패 (커밋/푸시 차단)
exit 1
```

### 2. 단계별 메시지

```bash
echo "Running pre-commit checks..."
echo "✓ Checking merge conflicts"
echo "✓ Validating code style"
echo "✓ Scanning for secrets"
echo "Pre-commit checks passed"
```

### 3. 사용자 확인

```bash
read -p "Continue? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
  exit 1
fi
```

### 4. 조건부 실행

```bash
# Java 파일만 검증
git diff --cached --name-only | grep '\.java$' | while read file; do
  # 검증 로직
done
```

## 디버깅

### Hook 수동 실행

```bash
# pre-commit 테스트
.githooks/pre-commit

# commit-msg 테스트
echo "feat: test message" | .githooks/commit-msg
```

### 상세 로그

```bash
# Hook 스크립트 시작 부분에 추가
set -x  # 명령어 출력
set -e  # 에러 시 즉시 종료
```

### 스크립트 문법 검증

```bash
# Bash 문법 체크
bash -n .githooks/pre-commit

# ShellCheck 사용 (설치 필요)
shellcheck .githooks/pre-commit
```

## 참고 자료

- [Git Hooks 공식 문서](https://git-scm.com/docs/githooks)
- [프로젝트 커밋 컨벤션](../CLAUDE.md#커밋-메시지)
- [Claude Code Hooks](.claude/hooks/README.md)

## 차이점: .githooks vs .claude/hooks

| 항목 | .githooks | .claude/hooks |
|------|-----------|---------------|
| 실행 주체 | Git | Claude Code |
| 실행 시점 | Git 이벤트 (commit, push) | Tool 사용 전후 |
| 언어 | Bash 스크립트 | Bash 스크립트 (JSON I/O) |
| 목적 | 코드 품질, 커밋 검증 | Claude 작업 검증, 제어 |
| 팀 적용 | 저장소 설정 필요 | Claude 설치 시 자동 |