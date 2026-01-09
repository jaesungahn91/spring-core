---
paths: "**/*.java"
---

# Issue-Driven Workflow

**CRITICAL**: 모든 코드 변경 작업은 반드시 GitHub Issue를 먼저 생성한 후 진행합니다.

## Claude의 필수 작업 순서

### 1. Issue 자동 생성 (작업 요청 받으면 즉시)

사용자가 코드 변경을 요청하고 Issue 번호가 없으면:

1. **즉시 GitHub Issue 생성**
   ```bash
   gh issue create \
     --title "[Type] 작업 제목" \
     --label "type:feature|bug|docs|tech-debt" \
     --label "module:..." \
     --label "priority:medium" \
     --body "작업 상세 내용"
   ```

2. **Issue 번호 저장 및 안내**
   - "Created Issue #123 for this work"
   - 생성된 Issue 번호를 컨텍스트에 포함

3. **작업 진행**
   - Issue 번호를 기준으로 작업 수행

### 2. 작업 수행

- 요청된 기능 구현/버그 수정
- 테스트 작성/실행
- 프로젝트 규칙 준수

### 3. 문서화

작업 완료 후 **반드시** 다음 문서 업데이트:
- README.md (새 기능/변경사항)
- API 문서 (새 엔드포인트/변경된 스펙)
- 모듈별 문서

### 4. 커밋

```bash
# 일반 커밋
git commit -m "type: 변경 내용 #<issue-number>"

# Issue 종료하는 커밋
git commit -m "type: 변경 내용 Closes #<issue-number>"
```

커밋 타입:
- `feat`: 새 기능
- `fix`: 버그 수정
- `docs`: 문서
- `refactor`: 리팩토링
- `test`: 테스트
- `chore`: 기타

### 5. Issue 업데이트 및 종료

```bash
# 작업 완료 코멘트
gh issue comment <issue-number> --body "작업 완료: [요약]"

# Issue 종료 (커밋 메시지에 Closes가 없는 경우)
gh issue close <issue-number>
```

## Issue 유형 및 라벨

### Type
- `type: bug` - 버그 수정
- `type: feature` - 새 기능
- `type: docs` - 문서
- `type: tech-debt` - 리팩토링

### Module
- `module: security-jwt`
- `module: data-jpa`
- `module: rest-docs`
- `module: data-mongodb`
- `module: batch`

### Priority
- `priority: high` - 긴급
- `priority: medium` - 보통 (기본값)
- `priority: low` - 낮음

## 예외: Issue 불필요한 작업

다음 작업은 Issue 없이 진행 가능:
- 오타/포맷팅
- 정보 조회 (파일 읽기, 검색)
- 빌드/테스트 실행 (수정 없음)
- 문서 확인

## 작업 예시

### ❌ 잘못된 방식
```
User: 사용자 인증 API 구현해줘
Claude: [바로 코드 작성 시작] ← 잘못됨!
```

### ✅ 올바른 방식
```
User: 사용자 인증 API 구현해줘
Claude:
1. GitHub Issue 생성 중...
   → Created Issue #123: [Feature] 사용자 인증 API 구현
2. 작업 진행: UserAuthController 구현
3. 테스트 작성: UserAuthControllerTest
4. 문서 업데이트: README.md, docs/api.md
5. 커밋: feat: 사용자 인증 API 추가 Closes #123
```

### ✅ Issue 번호가 이미 있는 경우
```
User: 사용자 인증 API 구현해줘 #123
Claude:
1. Issue #123 확인
2. 작업 진행...
```

## Don'ts

- ❌ Issue 생성 없이 코드 변경
- ❌ 커밋 메시지에 Issue 번호 누락
- ❌ 문서 업데이트 누락
- ❌ Issue 종료 없이 작업 완료 주장
- ❌ Issue와 무관한 변경 포함

## 관련 파일

- `.claude/agents/issue-workflow.md` - Issue 워크플로우 자동화
- `.claude/skills/create-issue/SKILL.md` - Issue 생성 참고
- `.github/ISSUE_TEMPLATE/` - Issue 템플릿