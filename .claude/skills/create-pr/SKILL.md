---
name: create-pr
description: Creates pull requests with proper descriptions. Use when the user wants to create a PR, submit code for review, or merge a branch into develop.
allowed-tools: Bash(git:*), Bash(gh:*), Read
---

# Create PR

## Steps

1. `git log develop...HEAD --oneline` → 변경 커밋 파악
2. `git diff --name-only develop...HEAD` → 변경 파일 파악
3. Read `templates/pr-body.md` → 내용 채워서 body 작성
4. `gh pr create --base develop --title "..." --body "..."` 실행
5. PR URL 반환

## Rules

- base 브랜치는 항상 `develop`
- title 형식: `[Type] 설명` (예: `[feat] 유저 API 추가`)
- 변경 파일 > 10개면 PR 분할 권장 후 사용자 확인 받기
- draft PR은 사용자가 명시적으로 요청할 때만