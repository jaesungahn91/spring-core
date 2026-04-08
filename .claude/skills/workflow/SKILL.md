---
name: workflow
description: 이슈 생성부터 PR 생성까지 전체 흐름을 오케스트레이션한다. (create-issue → 브랜치 생성 → quality-check → document-work → create-pr)
argument-hint: [feature description] | continue
allowed-tools: Bash(git:*), Bash(gh:*)
---

# Workflow

## Steps

### 시작: `/workflow "기능 설명"`

1. `create-issue` 스킬 호출 → 이슈 번호 획득
2. 이슈 제목에서 슬러그 생성 (소문자, 공백→하이픈, 특수문자 제거)
3. `git checkout -b feature/{이슈번호}-{슬러그}` 브랜치 생성
4. 사용자에게 안내 출력:
   > "브랜치 `feature/{번호}-{슬러그}` 생성 완료. 구현 후 `/workflow continue`를 실행하세요."
5. 대기

### 완료: `/workflow continue`

1. `quality-check` 스킬 호출
   - 실패 시: 실패 내용 출력 후 중단. 수정 후 다시 `/workflow continue` 실행 안내
   - 성공 시: 다음 단계 진행
2. `document-work` 스킬 호출 → 이슈 댓글 게시 + PR 본문용 작업 노트 수집
3. `create-pr` 스킬 호출 (작업 노트를 PR 본문에 포함)
4. PR URL 출력

## Rules

- `continue` 인자 없이 `/workflow`만 실행하면 기능 설명 입력 요청
- 브랜치가 이미 존재하면 오류 없이 해당 브랜치로 checkout
- quality-check는 변경된 모듈만 대상으로 실행 (git diff로 파악)
- 각 단계 실패 시 이후 단계 진행하지 않음