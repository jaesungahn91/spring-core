---
name: pr-document
description: 현재 브랜치의 작업 내용을 분석해 이슈 댓글과 PR 본문용 작업 노트를 생성한다.
allowed-tools: Bash(git:*), Bash(gh:*)
---

# Document Work

## Steps

1. `git log develop...HEAD --oneline` → 커밋 목록 수집
2. `git diff --stat develop...HEAD` → 변경 파일 목록 수집
3. 브랜치명에서 이슈 번호 파싱: `feature/{번호}-*` 패턴
4. 사용자에게 특이사항 입력 프롬프트 표시:
   > "작업 중 특이사항(발생한 문제, 해결 과정 등)이 있으면 입력하세요. 없으면 Enter."
5. 커밋 메시지 + 변경 파일 + 입력된 특이사항 + 현재 대화 컨텍스트를 종합해 작업 요약 작성:
   - 구현 내용
   - 변경 범위 (파일/패키지)
   - 발생한 문제 및 해결 과정 (있을 경우)
6. `gh issue comment {번호} --body "..."` 로 이슈에 완료 댓글 게시
7. PR 본문용 "작업 노트" 섹션 텍스트를 반환 (pr-create 또는 workflow 스킬에서 사용)

## Rules

- 이슈 번호를 브랜치명에서 파싱할 수 없으면 사용자에게 직접 확인
- 작업 요약은 간결하게, 불릿 포인트 위주
- 특이사항 없으면 "발생한 문제 및 해결 과정" 섹션 생략
- 이슈 댓글 게시 성공 후 이슈 URL 출력