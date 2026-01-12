#!/bin/bash
# SessionStart Hook: 프로젝트 규칙 파일 경로 안내

set -euo pipefail

# stdin에서 JSON 읽기
INPUT=$(cat)

# 현재 디렉토리
CWD=$(echo "$INPUT" | jq -r '.cwd // empty')
if [ -n "$CWD" ]; then
  cd "$CWD"
fi

# 프로젝트 규칙 경로 안내
MESSAGE="
📋 프로젝트 규칙 위치:
   • .claude/rules/workflow.md - Issue-Driven Workflow (CRITICAL)
   • .claude/rules/java-style.md - Java 코드 스타일
   • .claude/rules/README.md - Rules 개요
   • ~/.claude/CLAUDE.md - 글로벌 규칙 (자동 커밋 금지)
"

# JSON escape를 위해 jq 사용
ESCAPED_MESSAGE=$(echo "$MESSAGE" | jq -Rs .)
echo "{\"hookSpecificOutput\": {\"hookEventName\": \"SessionStart\", \"permissionDecision\": \"allow\", \"permissionDecisionReason\": ${ESCAPED_MESSAGE}}}"

exit 0