# Claude Code 설정

이 디렉토리는 Claude Code CLI의 프로젝트별 설정을 포함합니다.

## 디렉토리 구조

```
.claude/
├── settings.json              # 메인 설정 파일
├── settings.local.json        # 로컬 전용 설정 (gitignore)
│
├── rules/                     # 코딩 규칙 정의
│   ├── README.md
│   ├── java-style.md
│   ├── api-design.md
│   └── ...
│
├── agents/                    # 커스텀 에이전트
│   ├── README.md
│   ├── code-reviewer.md
│   └── test-fixer.md
│
├── commands/                  # 커스텀 명령어
│   ├── README.md
│   ├── build.md
│   └── test.md
│
├── hooks/                     # 이벤트 훅 스크립트
│   ├── README.md
│   ├── check-secrets.sh
│   └── validate-bash.sh
│
├── output-styles/             # 응답 스타일
│   ├── README.md
│   ├── concise-korean.md
│   └── teaching.md
│
└── skills/                    # 재사용 가능한 스킬
    └── run-module-tests/
```

## 주요 파일

### settings.json

프로젝트의 메인 설정 파일입니다.

```json
{
  "model": "claude-sonnet-4-5-20250929",
  "permissions": { ... },
  "hooks": { ... },
  "outputStyle": "Default"
}
```

**주요 설정:**
- `model`: 사용할 Claude 모델
- `permissions`: 도구 사용 권한 (allow/ask/deny)
- `hooks`: Pre/Post 이벤트 훅
- `outputStyle`: 응답 스타일
- `env`: 환경 변수

### settings.local.json

로컬 환경에서만 사용하는 설정입니다. (gitignore)

```json
{
  "permissions": {
    "allow": ["Bash(npm:*)"]
  }
}
```

## 구성 요소 설명

### 1. Rules

특정 파일 패턴에 적용되는 코딩 규칙입니다.

- **위치**: `rules/*.md`
- **용도**: 코드 작성 시 자동으로 참고되는 가이드라인
- **활성화**: frontmatter의 `paths` 패턴 매칭

[자세한 내용은 rules/README.md 참고]

### 2. Agents

복잡한 워크플로우를 자동화하는 전문 에이전트입니다.

- **위치**: `agents/*.md`
- **용도**: 코드 리뷰, 테스트 수정 등 특화 작업
- **특징**: 제한된 도구, 명확한 역할

[자세한 내용은 agents/README.md 참고]

### 3. Commands

자주 사용하는 작업을 간단한 명령어로 실행합니다.

- **위치**: `commands/*.md`
- **용도**: 빌드, 테스트 등 반복 작업
- **인자**: `$1`, `$2` 등 위치 기반

[자세한 내용은 commands/README.md 참고]

### 4. Hooks

도구 실행 전후에 자동으로 실행되는 스크립트입니다.

- **위치**: `hooks/*.sh`
- **용도**: 검증, 포맷팅, 보안 체크
- **이벤트**: PreToolUse, PostToolUse, SessionStart

[자세한 내용은 hooks/README.md 참고]

### 5. Output Styles

Claude의 응답 스타일을 커스터마이징합니다.

- **위치**: `output-styles/*.md`
- **용도**: 응답 톤, 형식, 길이 조정
- **활성화**: `settings.json`의 `outputStyle`

[자세한 내용은 output-styles/README.md 참고]

### 6. Skills

재사용 가능한 복합 작업입니다.

- **위치**: `skills/*/`
- **용도**: 프로젝트별 복잡한 워크플로우
- **구조**: 각 스킬은 독립 디렉토리

## 권한 시스템

`.claude/settings.json`의 `permissions`에서 도구 사용 권한을 제어합니다.

### Allow (자동 실행)
```json
"allow": [
  "Bash(git status:*)",
  "Read(src/**)",
  "Bash(./gradlew:*)"
]
```

### Ask (사용자 확인 필요)
```json
"ask": [
  "Bash(git push:*)",
  "Write(*.java)",
  "Edit(*.md)"
]
```

### Deny (차단)
```json
"deny": [
  "Read(.env)",
  "Write(.git/config)",
  "Bash(rm -rf:*)"
]
```

## 빠른 시작

### 1. 기본 설정 확인

```bash
cat .claude/settings.json
```

### 2. Rule 추가

```bash
# 새 rule 생성
cat > .claude/rules/my-rule.md << 'EOF'
---
paths: "*/src/**/*.java"
---

# My Rule
...
EOF
```

### 3. Hook 활성화

```bash
# Hook 스크립트에 실행 권한 부여
chmod +x .claude/hooks/*.sh

# settings.json에서 활성화 확인
```

### 4. Agent 실행

Claude에게 요청:
```
"코드 리뷰 해줘"
```

## 설정 우선순위

1. `settings.local.json` (로컬 전용)
2. `settings.json` (프로젝트)
3. `~/.claude/settings.json` (글로벌)

## 모범 사례

### DO
- 팀과 합의된 설정만 커밋
- 민감한 정보는 `settings.local.json`에
- Rule은 최소한으로 유지
- Hook은 빠르게 실행되도록 (<1초)

### DON'T
- API 키나 토큰을 settings에 하드코딩
- 너무 많은 Rule 추가 (선택적 적용)
- 복잡한 Hook 로직
- 모든 도구를 `allow`로 설정

## 문제 해결

### Hook 실행 안 됨
1. 실행 권한 확인: `chmod +x .claude/hooks/*.sh`
2. 스크립트 문법 확인: `bash -n script.sh`
3. 설정 확인: `settings.json`의 `hooks` 섹션

### Rule 적용 안 됨
1. Frontmatter `paths` 패턴 확인
2. 파일 경로와 패턴 매칭 테스트
3. YAML 문법 오류 확인

### Permission 거부됨
1. `settings.json`의 `permissions` 확인
2. 필요한 도구를 `allow` 또는 `ask`에 추가
3. `deny` 목록에서 제거

## 참고 자료

- [Claude Code 공식 문서](https://github.com/anthropics/claude-code)
- [프로젝트 CLAUDE.md](../CLAUDE.md)
- [글로벌 설정](~/.claude/CLAUDE.md)

## 기여

설정 개선 제안:
1. 로컬에서 테스트
2. 팀원과 논의
3. PR 생성
4. 리뷰 후 머지