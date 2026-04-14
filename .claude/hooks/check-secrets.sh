#!/bin/bash
# PreToolUse hook for Write/Edit operations
# Prevents writing secrets to files

INPUT=$(cat)

# Extract file content
CONTENT=$(echo "$INPUT" | jq -r '.tool_input.content // .tool_input.new_string // empty' 2>/dev/null || true)

# Exit if no content
if [ -z "$CONTENT" ]; then
  echo '{"hookSpecificOutput": {"hookEventName": "PreToolUse", "permissionDecision": "allow"}}'
  exit 0
fi

# Secret patterns
SECRET_PATTERNS=(
  "password\s*=\s*['\"][^'\"]{8,}"
  "api[_-]?key\s*=\s*['\"][^'\"]{16,}"
  "secret\s*=\s*['\"][^'\"]{16,}"
  "token\s*=\s*['\"][^'\"]{20,}"
)

# Check for secrets
for pattern in "${SECRET_PATTERNS[@]}"; do
  if echo "$CONTENT" | grep -qiE "$pattern"; then
    echo "{\"hookSpecificOutput\": {\"hookEventName\": \"PreToolUse\", \"permissionDecision\": \"deny\", \"permissionDecisionReason\": \"Potential secret detected. Use environment variables.\"}}" >&2
    exit 2
  fi
done

echo '{"hookSpecificOutput": {"hookEventName": "PreToolUse", "permissionDecision": "allow"}}'
exit 0