---
name: issue-update
description: 작업 중인 이슈의 Progress 섹션을 업데이트한다. 설계 변경, 발견 사항, 블로커, 범위 변경 등을 이슈 본문에 반영한다.
allowed-tools: Bash(gh:*)
---

# Update Issue

## Arguments

- `{number}` — 업데이트할 이슈 번호 (필수, 예: `/issue-update 73`)

## Steps

1. `gh auth status`로 active account 확인 → `jaesung-ahn`이 아니면 `gh auth switch --user jaesung-ahn` 실행
2. `gh issue view {number} --json body`로 현재 이슈 본문 조회
3. `## Progress` 섹션의 해당 항목에 내용 추가/수정
4. `gh issue edit {number} --body "..."` 로 본문 업데이트

## Rules

- Overview, Closing 섹션은 수정하지 않는다
- 기존 Progress 내용을 덮어쓰지 않고 누적한다
- 항목이 비어있으면 해당 항목만 채운다