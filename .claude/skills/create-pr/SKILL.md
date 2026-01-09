---
name: create-pr
description: Creates pull requests with comprehensive descriptions and checklists. Use when the user wants to create a PR or needs guidance on PR creation.
allowed-tools: Bash(git:*), Bash(gh:*), Read
---

# Create Pull Request

Pull Request를 체계적인 설명과 체크리스트로 생성합니다.

## When to Use

- 기능 개발 완료 후 PR 생성
- 버그 수정 후 PR 생성
- 코드 리뷰 요청 시
- develop 브랜치로 병합 준비 시

## Prerequisites Check

### 1. 브랜치 상태 확인
```bash
# 현재 브랜치
git branch --show-current

# 원격 브랜치와 sync 확인
git status
```

### 2. 변경 사항 분석
```bash
# develop과의 차이
git diff develop...HEAD

# 커밋 이력
git log develop...HEAD --oneline

# 변경된 파일 목록
git diff --name-only develop...HEAD
```

### 3. 테스트 실행
```bash
# 영향받는 모듈 테스트
cd spring-data-jpa
./gradlew test
```

### 4. 충돌 확인
```bash
git fetch origin develop
git merge-base HEAD origin/develop
```

## PR Body Template

```markdown
## Summary

[변경 사항을 3-5줄로 요약]

## Changes

### Added
- 새로 추가된 기능/파일

### Modified
- 수정된 기능/파일

### Removed
- 삭제된 기능/파일

## Motivation

[왜 이 변경이 필요한지]

## Related Issues

Closes #123
Relates to #456

## Type of Change

- [ ] feature (새 기능)
- [ ] fix (버그 수정)
- [ ] docs (문서)
- [ ] test (테스트)
- [ ] refactor (리팩토링)
- [ ] chore (기타)

## Affected Modules

- [ ] spring-security-jwt
- [ ] spring-data-jpa
- [ ] spring-rest-docs
- [ ] spring-data-mongodb
- [ ] spring-batch

## Test Plan

### Unit Tests
- [ ] 새 단위 테스트 추가
- [ ] 기존 단위 테스트 통과

### Integration Tests
- [ ] 새 통합 테스트 추가
- [ ] 기존 통합 테스트 통과

### Manual Testing
[수동 테스트 절차]

### Test Results

\`\`\`bash
cd spring-data-jpa
./gradlew test

BUILD SUCCESSFUL in 12s
5 actionable tasks: 5 executed
\`\`\`

## Checklist

- [ ] 코드가 프로젝트 컨벤션을 따름
- [ ] 자체 코드 리뷰 완료
- [ ] 테스트 통과
- [ ] 문서 업데이트 (필요 시)
- [ ] Breaking changes 없음
- [ ] 커밋 메시지 컨벤션 준수

## Screenshots (if applicable)

[스크린샷이나 GIF]

## Additional Notes

[리뷰어에게 전달할 추가 정보]
```

## Create PR Command

### Standard PR
```bash
# PR body를 파일로 저장
cat > /tmp/pr-body.md <<'EOF'
[위 템플릿 내용]
EOF

# PR 생성
gh pr create \
  --base develop \
  --title "[Feature] 유저 생성 API 추가" \
  --label "type: feature" \
  --label "module: data-jpa" \
  --body-file /tmp/pr-body.md
```

### Draft PR
```bash
gh pr create \
  --base develop \
  --title "[WIP] 유저 생성 API 작업중" \
  --label "type: feature" \
  --draft \
  --body-file /tmp/pr-body.md
```

### Auto-merge PR (after CI passes)
```bash
gh pr create \
  --base develop \
  --title "[Fix] 버그 수정" \
  --label "type: fix" \
  --body-file /tmp/pr-body.md

# PR 생성 후 auto-merge 설정
gh pr merge [PR-NUMBER] --auto --squash
```

## PR Title Convention

### Format
```
[Type] 간결한 설명
```

### Examples
```
[Feature] 유저 프로필 조회 API 추가
[Fix] 이메일 중복 검증 버그 수정
[Docs] API 문서 업데이트
[Refactor] UserService 리팩토링
[Test] 통합 테스트 추가
[Chore] Gradle 의존성 업데이트
```

## Review Checklist

### For Author (Self-Review)
1. 모든 변경 사항 확인
2. 불필요한 변경 제거 (디버그 코드, 콘솔 로그)
3. 테스트 커버리지 확인
4. 문서 업데이트 확인

### For Reviewer
1. 코드 품질 (가독성, 중복, 복잡도)
2. 아키텍처 패턴 준수
3. 보안 취약점
4. 성능 고려사항
5. 테스트 충분성

## Common Issues

### Merge Conflicts
```bash
# develop 최신화
git fetch origin develop
git rebase origin/develop

# 충돌 해결 후
git rebase --continue
git push --force-with-lease
```

### Failed CI
```bash
# 로컬에서 재현
./gradlew test

# 수정 후 재커밋
git add .
git commit --amend --no-edit
git push --force-with-lease
```

### Large PR
PR이 너무 크면 (변경 파일 > 10):
1. 논리적 단위로 분할
2. 첫 PR: 공통 모델/인터페이스
3. 두 번째 PR: 핵심 로직
4. 세 번째 PR: 테스트/문서

## Merge Strategy

### Squash Merge (권장)
- 모든 커밋을 하나로 합침
- develop 히스토리 깔끔
- PR 단위 리버트 용이

### Merge Commit
- 모든 커밋 보존
- 히스토리 복잡

### Rebase Merge
- 선형 히스토리
- 커밋 각각 보존

## After Merge

1. 브랜치 삭제
   ```bash
   git branch -d feature/user-api
   git push origin --delete feature/user-api
   ```

2. develop 동기화
   ```bash
   git checkout develop
   git pull origin develop
   ```

3. 관련 Issue 닫힘 확인

## Related Files

- `.github/pull_request_template.md` - PR 템플릿
- `.github/workflows/` - CI/CD 설정