# Claude Code Hooks

이 디렉토리는 Claude Code hooks 스크립트를 저장합니다.

## 사용 가능한 Hooks

| 스크립트 | 이벤트 | 목적 |
|---------|--------|------|
| `user-prompt-submit-hook.sh` | UserPromptSubmit | Issue-Driven Workflow 강제 (Issue 번호 필수) |
| `validate-bash.sh` | PreToolUse(Bash) | 위험한 Bash 명령어 차단 |
| `check-secrets.sh` | PreToolUse(Write/Edit) | 시크릿 하드코딩 방지 |
| `format-java.sh` | PostToolUse(Edit/Write) | Java 파일 자동 포맷팅 |
| `session-start.sh` | SessionStart | 세션 시작 시 환경 정보 표시 |

## 설정 방법

hooks는 `.claude/settings.json`에서 활성화합니다:

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": ".claude/hooks/validate-bash.sh",
            "timeout": 5000
          }
        ]
      },
      {
        "matcher": "Write|Edit",
        "hooks": [
          {
            "type": "command",
            "command": ".claude/hooks/check-secrets.sh",
            "timeout": 5000
          }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Write|Edit",
        "hooks": [
          {
            "type": "command",
            "command": ".claude/hooks/format-java.sh",
            "timeout": 10000
          }
        ]
      }
    ],
    "SessionStart": [
      {
        "hooks": [
          {
            "type": "command",
            "command": ".claude/hooks/session-start.sh",
            "timeout": 5000
          }
        ]
      }
    ]
  }
}
```

## Hook 입출력

### 입력 (stdin JSON)
```json
{
  "session_id": "abc123",
  "hook_event_name": "PreToolUse",
  "tool_name": "Bash",
  "tool_input": {
    "command": "npm test"
  },
  "cwd": "/path/to/project"
}
```

### 출력 (stdout JSON)
```json
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "allow",
    "permissionDecisionReason": "Command validated"
  }
}
```

### Exit Codes
- `0`: 성공 (계속 진행)
- `1`: 비블로킹 에러
- `2`: 블로킹 에러 (작업 중단)

## 개발 팁

### 디버깅
```bash
# Hook에 stdin을 직접 전달해서 테스트
echo '{"tool_input": {"command": "rm -rf /"}}' | .claude/hooks/validate-bash.sh
```

### 로깅
- `echo` 사용 시 `>&2`로 stderr에 출력
- Claude가 stderr 메시지를 사용자에게 표시

### 성능
- Timeout 설정 (기본 30초)
- 빠른 실행 유지 (< 1초 권장)