---
name: issue-create
description: Creates GitHub issues with proper formatting and labels. Use when the user wants to report a bug, request a feature, improve documentation, or track tech debt.
allowed-tools: Bash(gh:*), Read
---

# Create Issue

## Steps

1. `gh auth status`로 active account 확인 → `jaesung-ahn`이 아니면 `gh auth switch --user jaesung-ahn` 실행
2. Read `labels.md` → 이슈 유형 및 적합한 라벨 결정
3. body가 없으면 Read `templates/{type}.md` → 내용 채우기
4. `gh issue create --title "..." --label "..." --body "..."` 실행
5. 생성된 이슈 번호 반환

## Rules

- title 형식: `[type] 설명` (예: `[feat] 유저 API 추가`, `[fix] NPE 수정`)
- `type:` 라벨 필수, 모듈 코드 변경 시 `module:` 라벨 필수
- body는 마크다운, 각 섹션 간결하게 작성
- 라벨이 존재하지 않을 수 있으므로 `gh label list`로 확인 후 사용
