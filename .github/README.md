# GitHub 설정

이 디렉토리는 GitHub 관련 설정과 자동화를 포함합니다.

## 디렉토리 구조

```
.github/
├── workflows/                    # GitHub Actions
│   ├── ci-build.yml
│   ├── ci-test.yml
│   ├── code-quality.yml
│   ├── release-drafter.yml
│   └── stale.yml
│
├── ISSUE_TEMPLATE/               # 이슈 템플릿
│   ├── bug_report.yml
│   ├── feature_request.yml
│   ├── tech_debt.yml
│   └── docs_improvement.yml
│
├── pull_request_template.md     # PR 템플릿
├── CODEOWNERS                   # 코드 소유자
├── CONTRIBUTING.md              # 기여 가이드
├── SECURITY.md                  # 보안 정책
├── dependabot.yml               # Dependabot 설정
├── auto-assign.yml              # PR 자동 할당
├── labels.json                  # 라벨 정의
└── release-drafter.yml          # 릴리스 노트 설정
```

## Workflows

### ci-build.yml
각 모듈을 병렬로 빌드합니다.

**트리거:**
- `push` to `develop`, `main`
- `pull_request`

**Jobs:**
- 모듈별 빌드
- Java 11/17 버전별 실행
- 빌드 아티팩트 업로드

**매트릭스:**
```yaml
strategy:
  matrix:
    module: [spring-security-jwt, spring-data-jpa, ...]
    java-version: [11, 17]
```

### ci-test.yml
각 모듈의 테스트를 실행합니다.

**트리거:**
- `push` to `develop`, `main`
- `pull_request`

**Jobs:**
- 단위 테스트
- 통합 테스트
- 테스트 커버리지 리포트

### code-quality.yml
코드 품질을 검사합니다.

**트리거:**
- `pull_request`

**Jobs:**
- Checkstyle
- SpotBugs
- PMD
- 코드 포맷팅 검증

### release-drafter.yml
릴리스 노트를 자동 생성합니다.

**트리거:**
- `push` to `main`

**기능:**
- 커밋 메시지 기반 분류
- 버전 번호 자동 증가
- 변경사항 요약

### stale.yml
오래된 이슈/PR을 관리합니다.

**트리거:**
- 스케줄 (매일)

**동작:**
- 60일 미활동 → stale 라벨
- 7일 추가 미활동 → 자동 닫기

## Issue Templates

### bug_report.yml
버그 리포트 템플릿

**필드:**
- 버그 설명
- 재현 단계
- 예상 동작
- 실제 동작
- 환경 정보

### feature_request.yml
기능 요청 템플릿

**필드:**
- 기능 설명
- 동기 및 배경
- 제안 구현
- 대안
- 우선순위

### tech_debt.yml
기술 부채 개선 템플릿

**필드:**
- 현재 상태
- 문제점
- 개선 방향
- 영향 범위

### docs_improvement.yml
문서 개선 템플릿

**필드:**
- 문서 위치
- 현재 문제
- 개선 제안

## Pull Request Template

PR 생성 시 자동으로 적용되는 템플릿입니다.

**구조:**
```markdown
## Summary
[변경 사항 요약]

## Changes
- [ ] 변경 1
- [ ] 변경 2

## Test Plan
- [ ] 단위 테스트
- [ ] 통합 테스트
- [ ] 수동 테스트

## Screenshots
[선택사항]

## Checklist
- [ ] 테스트 통과
- [ ] 문서 업데이트
- [ ] 코드 리뷰 완료
```

## CODEOWNERS

코드 소유자를 정의합니다. PR 생성 시 자동으로 리뷰어가 할당됩니다.

```
# 전체 프로젝트
* @team-lead

# 특정 모듈
/spring-security-jwt/ @security-team
/spring-data-jpa/ @backend-team

# 문서
*.md @docs-team
```

## Dependabot

의존성 자동 업데이트 설정입니다.

```yaml
version: 2
updates:
  - package-ecosystem: gradle
    directory: "/"
    schedule:
      interval: weekly
    open-pull-requests-limit: 10
```

**기능:**
- 주간 의존성 체크
- 자동 PR 생성
- 보안 업데이트 우선

## Auto Assign

PR 생성 시 자동으로 리뷰어를 할당합니다.

```yaml
addReviewers: true
addAssignees: true
reviewers:
  - reviewer1
  - reviewer2
numberOfReviewers: 2
```

## Labels

프로젝트에서 사용하는 라벨 목록입니다.

```json
[
  {
    "name": "bug",
    "color": "d73a4a",
    "description": "Something isn't working"
  },
  {
    "name": "enhancement",
    "color": "a2eeef",
    "description": "New feature or request"
  }
]
```

**카테고리:**
- **Type**: `bug`, `enhancement`, `docs`, `test`
- **Priority**: `priority:high`, `priority:medium`, `priority:low`
- **Status**: `in-progress`, `blocked`, `ready-to-review`
- **Module**: `module:security`, `module:jpa`, ...

## 커스터마이징

### 새 Workflow 추가

```bash
# 템플릿 생성
cat > .github/workflows/my-workflow.yml << 'EOF'
name: My Workflow

on:
  push:
    branches: [develop]

jobs:
  my-job:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run something
        run: echo "Hello"
EOF
```

### Issue Template 추가

```bash
# 템플릿 생성
cat > .github/ISSUE_TEMPLATE/custom.yml << 'EOF'
name: Custom Template
description: Custom issue template
labels: ["custom"]
body:
  - type: input
    id: summary
    attributes:
      label: Summary
      description: Brief summary
    validations:
      required: true
EOF
```

### Label 추가

```bash
# labels.json에 추가
[
  ...
  {
    "name": "my-label",
    "color": "ffffff",
    "description": "My custom label"
  }
]

# GitHub에 적용 (gh CLI 필요)
gh label create "my-label" --color "ffffff" --description "My custom label"
```

## 모범 사례

### Workflows

**DO:**
- 모듈별 병렬 빌드로 시간 단축
- 캐싱 활용 (Gradle, dependencies)
- 실패 시 빠른 피드백
- Secrets 사용 (환경변수 노출 금지)

**DON'T:**
- 너무 긴 워크플로우 (>30분)
- 불필요한 트리거
- 민감한 정보 로그 출력
- 실패해도 계속 진행 (continue-on-error 남용)

### Issue Templates

**DO:**
- 필수 정보 명확히
- 선택 필드는 최소화
- 라벨 자동 할당
- 구체적인 가이드

**DON'T:**
- 너무 많은 필드
- 모호한 질문
- 중복된 템플릿

### CODEOWNERS

**DO:**
- 팀별 책임 영역 명확히
- 정기적 업데이트
- 최소 2명 이상 리뷰어

**DON'T:**
- 한 사람에게 모든 코드
- 너무 세분화된 소유권
- 비활동 멤버 포함

## 문제 해결

### Workflow 실패

1. **로그 확인**
   ```bash
   gh run view [run-id] --log
   ```

2. **로컬 재현**
   ```bash
   # act 사용 (로컬 GitHub Actions)
   act -j my-job
   ```

3. **디버깅 모드**
   ```yaml
   - name: Debug
     run: |
       echo "DEBUG INFO"
       env
   ```

### Dependabot PR 충돌

1. **재베이스**
   ```bash
   @dependabot rebase
   ```

2. **무시**
   ```bash
   @dependabot ignore this dependency
   ```

### Label 동기화

```bash
# labels.json에서 GitHub로 동기화
gh label create --force -f .github/labels.json
```

## 보안

### Secrets 관리

```yaml
# Workflow에서 사용
env:
  MY_SECRET: ${{ secrets.MY_SECRET }}
```

**추가 방법:**
- Repository Settings → Secrets → New secret
- Organization level secrets 공유 가능

### 보안 취약점 보고

`.github/SECURITY.md`에 정의된 절차를 따르세요.

1. 공개 이슈 생성 금지
2. security@example.com으로 비공개 보고
3. 24시간 내 응답 예상

## 참고 자료

- [GitHub Actions 문서](https://docs.github.com/actions)
- [Issue Template 문법](https://docs.github.com/communities/using-templates)
- [CODEOWNERS 문법](https://docs.github.com/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners)
- [Dependabot 설정](https://docs.github.com/code-security/dependabot)