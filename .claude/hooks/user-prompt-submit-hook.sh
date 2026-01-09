#!/bin/bash
# Issue-Driven Workflow: 작업 요청 시 자동으로 Issue 생성 유도

set -euo pipefail

# stdin에서 JSON 읽기
INPUT=$(cat)

# 사용자 프롬프트 추출
PROMPT=$(echo "$INPUT" | jq -r '.user_prompt // empty')

if [ -z "$PROMPT" ]; then
  # 프롬프트가 없으면 통과
  echo '{"hookSpecificOutput": {"hookEventName": "UserPromptSubmit", "permissionDecision": "allow"}}'
  exit 0
fi

# 코드 변경 관련 키워드 감지
if echo "$PROMPT" | grep -qiE "(구현|추가|수정|삭제|제거|fix|feat|refactor|작성|변경|개선|최적화|생성)"; then
  # Issue 번호 확인 (#숫자 형태)
  if ! echo "$PROMPT" | grep -qE "#[0-9]+"; then
    # Issue 번호가 없으면 자동 생성을 안내 (차단하지 않음)
    echo '{"hookSpecificOutput": {"hookEventName": "UserPromptSubmit", "permissionDecision": "allow", "permissionDecisionReason": "ℹ️  Issue-Driven Workflow가 활성화되어 있습니다.\nGitHub Issue를 자동으로 생성한 후 작업을 진행합니다."}}' | jq -c
    exit 0
  fi
fi

# Issue 번호가 있거나 코드 변경이 아닌 경우 통과
echo '{"hookSpecificOutput": {"hookEventName": "UserPromptSubmit", "permissionDecision": "allow"}}'
exit 0