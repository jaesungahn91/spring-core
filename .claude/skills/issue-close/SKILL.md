---
name: issue-close
description: 이슈의 Closing 섹션을 채우고 이슈를 닫는다. 결과 요약, 관련 PR, 후속 이슈를 정리한 뒤 closed 상태로 변경한다.
allowed-tools: Bash(gh:*)
---

# Close Issue

## Arguments

- `{number}` — 닫을 이슈 번호 (필수, 예: `/issue-close 73`)

## Steps

1. `gh auth status`로 active account 확인 → `jaesung-ahn`이 아니면 `gh auth switch --user jaesung-ahn` 실행
2. `gh issue view {number} --json body`로 현재 이슈 본문 조회
3. `## Closing` 섹션의 항목 채우기
   - 결과 요약: 완료된 작업 요약
   - 관련 PR: 연결된 PR 번호
   - 후속 이슈: 이번 범위에서 제외된 항목 (없으면 `없음`)
4. `gh issue edit {number} --body "..."` 로 본문 업데이트
5. `gh issue close {number}` 로 이슈 닫기

## Rules

- Overview, Progress 섹션은 수정하지 않는다
- 후속 이슈가 있으면 별도 이슈로 생성할지 사용자에게 확인한다