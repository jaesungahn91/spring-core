---
name: create-issue
description: Creates GitHub issues with proper formatting and labels. Use when the user wants to report a bug, request a feature, or create documentation tasks.
allowed-tools: Bash(gh:*), Read
---

# Create GitHub Issue

GitHub Issue를 적절한 포맷과 라벨로 생성합니다.

## When to Use

- 버그 발견 시
- 새 기능 제안 시
- 문서 개선 필요 시
- 기술 부채/리팩토링 작업 필요 시

## Issue Types

### Bug Report
```bash
gh issue create \
  --title "[Bug] 유저 생성 시 이메일 중복 검증 실패" \
  --label "type: bug" \
  --label "module: data-jpa" \
  --label "priority: high" \
  --body "$(cat <<'EOF'
## 버그 설명
유저 생성 API 호출 시 이미 존재하는 이메일로 요청하면 500 에러 발생

## 재현 방법
1. POST /users {"email": "existing@example.com", "password": "test123"}
2. 같은 요청 재시도
3. 500 Internal Server Error 발생

## 예상 동작
409 Conflict 응답과 명확한 에러 메시지

## 실제 동작
500 에러와 스택 트레이스

## 환경
- Spring Boot: 2.7.18
- Java: 11
- 모듈: spring-data-jpa
EOF
)"
```

### Feature Request
```bash
gh issue create \
  --title "[Feature] 유저 프로필 이미지 업로드 기능" \
  --label "type: feature" \
  --label "module: data-jpa" \
  --label "priority: medium" \
  --body "$(cat <<'EOF'
## 문제 또는 필요성
현재 유저는 텍스트 정보만 저장 가능하며, 프로필 이미지 기능이 없음

## 제안하는 해결책
1. 이미지 파일 업로드 API 추가
2. S3 또는 로컬 스토리지 저장
3. User 엔티티에 profileImageUrl 필드 추가

## 대안
- Gravatar 통합
- 외부 이미지 URL 저장

## 추가 정보
파일 크기 제한: 5MB
지원 형식: jpg, png, gif
EOF
)"
```

### Documentation
```bash
gh issue create \
  --title "[Docs] API 문서 업데이트 필요" \
  --label "type: docs" \
  --label "priority: low" \
  --body "$(cat <<'EOF'
## 문서 업데이트 내용
- 새로 추가된 엔드포인트 문서화
- 에러 응답 형식 명확화
- 인증 방법 예제 추가

## 위치
- /docs/api.md
- README.md
EOF
)"
```

### Tech Debt / Refactoring
```bash
gh issue create \
  --title "[Tech Debt] UserService 리팩토링" \
  --label "type: tech-debt" \
  --label "module: data-jpa" \
  --label "priority: medium" \
  --body "$(cat <<'EOF'
## 문제
UserService 클래스가 300줄 초과, 여러 책임 혼재

## 개선 방안
1. UserValidator로 검증 로직 분리
2. UserMapper로 DTO 변환 분리
3. UserNotificationService로 알림 로직 분리

## 예상 효과
- 테스트 용이성 증가
- 코드 가독성 향상
- 단일 책임 원칙 준수
EOF
)"
```

## Labels

### Type Labels
- `type: bug` - 버그 수정
- `type: feature` - 새 기능
- `type: docs` - 문서
- `type: test` - 테스트
- `type: chore` - 기타
- `type: tech-debt` - 리팩토링

### Module Labels
- `module: security-jwt`
- `module: data-jpa`
- `module: rest-docs`
- `module: data-mongodb`
- `module: batch`

### Priority Labels
- `priority: high` - 긴급
- `priority: medium` - 보통
- `priority: low` - 낮음

## Workflow

1. **Issue 유형 결정**
   - 버그? 기능? 문서? 리팩토링?

2. **필수 정보 수집**
   - 버그: 재현 방법, 예상/실제 동작
   - 기능: 필요성, 해결책, 대안
   - 문서: 업데이트 내용, 위치
   - 리팩토링: 문제, 개선 방안

3. **라벨 선택**
   - Type (필수)
   - Module (영향받는 모듈)
   - Priority (우선순위)

4. **gh CLI 명령어 실행**
   ```bash
   gh issue create --title "[Type] 제목" --label "..." --body "..."
   ```

5. **Issue 번호 확인**
   - 생성된 Issue 번호를 브랜치명/커밋에 활용

## Tips

- 제목: `[Type] 간결한 설명` 형식
- Body: 마크다운 포맷 사용
- 재현 가능한 최소 예제 포함
- 관련 파일 경로 명시
- 스크린샷/로그 첨부 (필요 시)

## Related Files

- `.github/ISSUE_TEMPLATE/` - Issue 템플릿
- `.github/labels.json` - 라벨 정의