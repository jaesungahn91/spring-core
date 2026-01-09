# Agents

프로젝트 특화 커스텀 에이전트 정의입니다.

## 개요

Agents는 특정 작업을 자동화하는 전문화된 AI 어시스턴트입니다. 각 에이전트는 특정 도구만 사용하며, 명확한 역할과 권한을 갖습니다.

## 현재 정의된 Agents

| Agent | 설명 | 사용 도구 | 사용 시점 |
|-------|------|----------|----------|
| `code-reviewer` | Spring Boot 코드 리뷰 전문 | Read, Grep, Glob, git | 코드 변경 후 |
| `test-fixer` | 테스트 실패 진단 및 수정 | Read, Edit, gradle, git | 테스트 실패 시 |

## 사용 방법

Claude가 자동으로 적절한 상황에서 에이전트를 실행하거나, 명시적으로 호출할 수 있습니다:

```bash
# 코드 리뷰
"코드 리뷰 해줘"

# 테스트 수정
"테스트 고쳐줘"
```

## Agent 정의 구조

각 agent는 frontmatter와 본문으로 구성됩니다:

```markdown
---
name: my-agent
description: What this agent does
tools: Read, Write, Bash(git:*)
model: sonnet
permissionMode: default
---

# Agent Instructions

에이전트가 수행할 작업과 절차를 여기에 작성합니다.

## Process
1. 첫 번째 단계
2. 두 번째 단계

## Output Format
결과를 어떻게 제공할지 정의합니다.
```

## Frontmatter 필드

| 필드 | 필수 | 설명 | 예시 |
|------|------|------|------|
| `name` | ✅ | 에이전트 이름 | `code-reviewer` |
| `description` | ✅ | 에이전트 설명 | `Expert code reviewer...` |
| `tools` | ✅ | 허용된 도구 목록 | `Read, Bash(git:*)` |
| `model` | ❌ | 사용할 모델 | `sonnet`, `haiku` |
| `permissionMode` | ❌ | 권한 모드 | `default`, `restrictive` |

## Tools 지정 방법

### 전체 도구 허용
```yaml
tools: Read, Write, Edit
```

### 특정 명령만 허용
```yaml
tools: Bash(gradle:*), Bash(git:*)
```

### 와일드카드 사용
```yaml
tools: Read(src/**), Edit(src/**/*)
```

## Agent 추가 예제

### 예제 1: 문서화 전문 에이전트

```markdown
---
name: doc-writer
description: Generates and updates project documentation
tools: Read, Write(*.md), Bash(git:*)
model: sonnet
---

# Documentation Writer

프로젝트 문서화 전문 에이전트입니다.

## Process
1. 코드 분석 (Read)
2. 기존 문서 확인
3. 문서 생성/업데이트
4. 변경사항 요약

## Output
- 생성/업데이트된 파일 목록
- 주요 변경 내용
```

### 예제 2: 성능 테스트 전문 에이전트

```markdown
---
name: perf-tester
description: Runs performance tests and analyzes results
tools: Bash(./gradlew:*), Read(build/reports/**)
model: sonnet
---

# Performance Tester

성능 테스트 실행 및 분석 전문 에이전트입니다.

## Process
1. 성능 테스트 실행
2. 결과 파일 분석
3. 병목 지점 식별
4. 개선 제안

## Metrics
- Response time (p50, p95, p99)
- Throughput (req/sec)
- Error rate
```

## 모범 사례

### DO
- 명확한 책임 범위
- 최소 권한 원칙 (필요한 도구만)
- 구체적인 프로세스 정의
- 일관된 출력 포맷

### DON'T
- 모든 도구 권한 부여
- 모호한 역할 정의
- 너무 복잡한 로직
- 다른 에이전트와 역할 중복

## 디버깅

에이전트 실행 중 문제가 발생하면:

1. **도구 권한 확인**: `tools` 필드에 필요한 도구 포함 여부
2. **프로세스 단순화**: 단계를 명확하게 분리
3. **로그 확인**: Claude의 출력에서 에이전트 실행 로그 확인

## 참고

- 에이전트는 `.claude/settings.json`의 permissions를 따릅니다
- 복잡한 작업은 여러 단계로 분리하는 것이 좋습니다
- 에이전트 간에는 체이닝이 불가능합니다 (순차 실행만)