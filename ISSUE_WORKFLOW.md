# Issue-Driven Workflow

프로젝트에서 Issue 기반 개발 워크플로우를 강제하는 자동화 시스템입니다.

## 개요

모든 코드 변경 작업은 다음 순서로 진행됩니다:

1. **GitHub Issue 생성** (자동)
2. **작업 수행**
3. **문서화**
4. **커밋** (Issue 번호 포함)
5. **Issue 종료**

## 작동 방식

### 사용자 → Claude Code

```
User: 사용자 인증 API 구현해줘
```

### Claude Code → 자동 워크플로우

```
1. ℹ️  Issue-Driven Workflow 활성화 감지
2. 🔨 GitHub Issue 자동 생성
   → Created Issue #123: [Feature] 사용자 인증 API 구현
3. 💻 작업 수행
   - UserAuthController.java 구현
   - UserAuthControllerTest.java 테스트 작성
   - 테스트 실행 및 통과 확인
4. 📝 문서화
   - README.md 업데이트 (API 엔드포인트 추가)
   - docs/api.md 업데이트 (인증 흐름 문서화)
5. 📦 커밋
   - git commit -m "feat: 사용자 인증 API 추가 Closes #123"
6. ✅ Issue 자동 종료
   - 커밋 메시지의 "Closes #123"으로 자동 종료
```

## 구성 요소

### 1. Hook: `user-prompt-submit-hook.sh`

**위치**: `.claude/hooks/user-prompt-submit-hook.sh`

**역할**: 작업 요청 시 Issue-Driven Workflow 알림

**동작**:
- 코드 변경 키워드 감지 (구현, 추가, 수정, fix, feat, refactor 등)
- Issue 번호가 없으면 자동 생성 안내 메시지 표시
- 작업 차단하지 않음 (allow)

### 2. Rule: `workflow.md`

**위치**: `.claude/rules/workflow.md`

**역할**: Claude에게 Issue-Driven Workflow 지시

**핵심 내용**:
- Issue 번호 없으면 자동으로 Issue 생성 필수
- 작업 순서 정의
- 커밋 메시지 포맷
- 문서화 필수 항목

### 3. Agent: `issue-workflow.md`

**위치**: `.claude/agents/issue-workflow.md`

**역할**: 전체 워크플로우 자동화 로직

**기능**:
- Issue 자동 생성 (gh CLI 사용)
- 작업 진행 상황 Issue 업데이트
- 문서 자동 업데이트
- Issue 종료

### 4. Skill: `create-issue`

**위치**: `.claude/skills/create-issue/SKILL.md`

**역할**: GitHub Issue 생성 템플릿 제공

**Issue 유형**:
- Bug Report: `type: bug`
- Feature Request: `type: feature`
- Documentation: `type: docs`
- Tech Debt: `type: tech-debt`

## Issue 유형 및 라벨

### Type Labels (필수)
- `type: bug` - 버그 수정
- `type: feature` - 새 기능
- `type: docs` - 문서
- `type: test` - 테스트
- `type: chore` - 기타
- `type: tech-debt` - 리팩토링

### Module Labels (영향받는 모듈)
- `module: security-jwt`
- `module: data-jpa`
- `module: rest-docs`
- `module: data-mongodb`
- `module: batch`

### Priority Labels
- `priority: high` - 긴급
- `priority: medium` - 보통 (기본값)
- `priority: low` - 낮음

## 사용 예시

### 예시 1: 새 기능 추가

```
User: 유저 프로필 조회 API 추가해줘

Claude:
1. Creating GitHub Issue...
   → Issue #124: [Feature] 유저 프로필 조회 API
   → Labels: type: feature, module: data-jpa, priority: medium
   → URL: https://github.com/owner/repo/issues/124

2. Implementing UserProfileController...
   ✅ src/.../UserProfileController.java created
   ✅ src/.../UserProfileControllerTest.java created
   ✅ Tests passed

3. Updating documentation...
   ✅ README.md updated
   ✅ docs/api.md updated

4. Committing changes...
   ✅ git commit -m "feat: 유저 프로필 조회 API 추가 Closes #124"

5. Issue #124 closed automatically via commit message
```

### 예시 2: 버그 수정

```
User: NPE 발생하는 버그 수정해줘 (UserService.findById)

Claude:
1. Creating GitHub Issue...
   → Issue #125: [Bug] UserService.findById NPE 발생
   → Labels: type: bug, module: data-jpa, priority: high

2. Analyzing bug...
   → Found NPE at UserService.java:45

3. Fixing bug...
   ✅ Added null check
   ✅ Added test for edge case

4. Updating documentation...
   ✅ Added troubleshooting section

5. Committing...
   ✅ git commit -m "fix: UserService.findById NPE 수정 Closes #125"
```

### 예시 3: Issue 번호가 이미 있는 경우

```
User: 유저 프로필 조회 API 추가해줘 #124

Claude:
1. Using existing Issue #124
2. Implementing feature...
   [작업 진행]
```

## 예외: Issue 불필요한 작업

다음 작업은 Issue 없이 진행 가능:

- 오타/포맷팅 수정
- 파일 읽기, 검색
- 빌드/테스트 실행 (코드 수정 없음)
- 문서 확인

```
User: README.md 읽어줘
Claude: [Issue 생성 없이 바로 실행]

User: 빌드 실행해줘
Claude: [Issue 생성 없이 바로 실행]
```

## 커밋 메시지 형식

### 일반 커밋
```bash
git commit -m "type: 변경 내용 요약 #<issue-number>"
```

### Issue 종료 커밋
```bash
git commit -m "type: 변경 내용 요약 Closes #<issue-number>"
```

### 커밋 타입
- `feat`: 새 기능
- `fix`: 버그 수정
- `docs`: 문서만 변경
- `refactor`: 리팩토링
- `test`: 테스트 추가/수정
- `chore`: 빌드, 설정 등

### 예시
```bash
# Feature
git commit -m "feat: 사용자 인증 API 추가 Closes #123"

# Bug fix
git commit -m "fix: NPE 수정 Closes #125"

# Documentation
git commit -m "docs: API 문서 업데이트 #126"

# Refactoring
git commit -m "refactor: UserService 리팩토링 #127"
```

## 문서화 필수 항목

작업 완료 후 다음 문서를 업데이트해야 합니다:

### README.md
- 새 기능 추가 시 사용법
- 변경된 API 엔드포인트
- 설치/설정 변경사항

### docs/api.md (또는 모듈별 API 문서)
- 새 엔드포인트 스펙
- Request/Response 예제
- 에러 코드

### 모듈별 README
- 모듈 특화 변경사항
- 의존성 변경

## 트러블슈팅

### Issue 생성 실패

**문제**: `gh: command not found`

**해결**:
```bash
brew install gh
gh auth login
```

---

**문제**: 라벨이 없어서 Issue 생성 실패

**해결**:
```bash
# 라벨 확인
gh label list

# 라벨 생성
gh label create "type: feature" --color "0052CC"
gh label create "type: bug" --color "D73A4A"
```

### Hook이 작동하지 않음

**문제**: Hook 실행 권한 없음

**해결**:
```bash
chmod +x .claude/hooks/user-prompt-submit-hook.sh
```

---

**문제**: Hook이 무시됨

**해결**: `.claude/settings.json` 확인
```json
{
  "disableAllHooks": false,
  "hooks": {
    "UserPromptSubmit": [...]
  }
}
```

### Claude가 Issue를 생성하지 않음

**문제**: workflow rule이 적용되지 않음

**해결**: `.claude/rules/workflow.md`의 frontmatter 확인
```markdown
---
paths: "**/*.java"
---
```

Java 파일 작업 시에만 적용됩니다. 다른 파일 유형에도 적용하려면:
```markdown
---
paths: "**/*"
---
```

## 설정 파일 목록

| 파일 | 역할 |
|------|------|
| `.claude/hooks/user-prompt-submit-hook.sh` | 작업 요청 시 알림 |
| `.claude/rules/workflow.md` | Claude 행동 지시 |
| `.claude/agents/issue-workflow.md` | 워크플로우 자동화 |
| `.claude/skills/create-issue/SKILL.md` | Issue 생성 템플릿 |
| `.claude/settings.json` | Hook 설정 |
| `.github/ISSUE_TEMPLATE/` | GitHub Issue 템플릿 |

## 비활성화

### 임시 비활성화

`.claude/settings.json`:
```json
{
  "disableAllHooks": true
}
```

### 완전 제거

```bash
# Hook 비활성화
rm .claude/hooks/user-prompt-submit-hook.sh

# Rule 비활성화
mv .claude/rules/workflow.md .claude/rules/workflow.md.disabled

# settings.json에서 hook 제거
```

## 참고

- [GitHub CLI 문서](https://cli.github.com/manual/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Claude Code Hooks 문서](.claude/hooks/README.md)
- [Claude Code Rules 문서](.claude/rules/README.md)
- [Claude Code Agents 문서](.claude/agents/README.md)