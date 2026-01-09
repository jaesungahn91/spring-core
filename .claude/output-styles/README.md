# Output Styles

Claude의 응답 스타일을 정의합니다.

## 개요

Output Styles는 Claude가 응답하는 방식을 커스터마이징합니다. 프로젝트나 팀의 선호도에 맞춰 응답 형식, 톤, 길이 등을 조정할 수 있습니다.

## 현재 정의된 Styles

| Style | 설명 | 대상 |
|-------|------|------|
| `concise-korean` | 간결한 한국어 응답 | 숙련된 개발자 |
| `teaching` | 교육적이고 상세한 설명 | 학습 중인 개발자 |

## 활성화 방법

`.claude/settings.json`에서 설정:

```json
{
  "outputStyle": "concise-korean"
}
```

또는 대화 중 요청:
```
"간결하게 답해줘"
"자세히 설명해줘"
```

## Style 정의 구조

```markdown
---
name: Style Name
description: What this style does
keep-coding-instructions: true
---

# Style Name

스타일 설명

## Core Principles
1. 원칙 1
2. 원칙 2

## Response Format

[응답 포맷 예시]

## Examples

### ❌ Before
[기본 스타일]

### ✅ After
[적용 후 스타일]
```

## Frontmatter 필드

| 필드 | 필수 | 설명 |
|------|------|------|
| `name` | ✅ | 스타일 이름 |
| `description` | ✅ | 스타일 설명 |
| `keep-coding-instructions` | ❌ | 코딩 관련 지시사항 유지 여부 (기본: false) |

## 커스텀 Style 추가 예제

### 예제 1: 영어 전문 용어 스타일

```markdown
---
name: Technical English
description: Uses English technical terms with Korean explanations
keep-coding-instructions: true
---

# Technical English Output Style

전문 용어는 영어로, 설명은 한국어로 작성하는 스타일입니다.

## Core Principles

1. **Technical Terms**: 영어 원문 사용
   - Repository, Entity, Service 등
2. **Explanations**: 한국어로 설명
3. **Code**: 영어 주석

## Examples

### ❌ Before
```
저장소 계층에서 엔티티를 조회합니다.
```

### ✅ After
```
Repository layer에서 Entity를 조회합니다.
```

## Response Format

```markdown
## [Technical Term]

[한국어 설명]

Code:
// English comments
public class Example {
    // ...
}
```
```

### 예제 2: 시니어 개발자 스타일

```markdown
---
name: Senior Developer
description: Minimal output assuming high expertise
keep-coding-instructions: true
---

# Senior Developer Output Style

최소한의 응답으로 핵심만 전달합니다.

## Core Principles

1. **Assumptions**: 기본 지식 가정
2. **Direct**: 바로 핵심으로
3. **Trade-offs**: 선택지와 트레이드오프
4. **No Hand-holding**: 상세한 설명 생략

## Response Format

### Code Changes
```diff
- old code
+ new code
```

Reason: [한 줄]

### Architecture Decisions
```
Option A: pros/cons
Option B: pros/cons

→ A (why)
```

### Bug Diagnosis
```
Root cause: [one line]

Fix:
[code only]
```

## Examples

### ❌ Verbose
```
NullPointerException이 발생했습니다.
이는 userRepository.findById()가 Optional.empty()를 반환할 때
.get()을 호출해서 발생합니다.
Optional의 orElseThrow()를 사용하면 해결됩니다.
```

### ✅ Senior Style
```
NPE: Optional.get()

Fix:
return userRepository.findById(id)
    .orElseThrow(() -> new UserNotFoundException(id));
```
```

### 예제 3: 코드 리뷰 스타일

```markdown
---
name: Code Review
description: Structured code review format
keep-coding-instructions: true
---

# Code Review Output Style

체계적인 코드 리뷰 형식입니다.

## Response Format

```markdown
## Summary
[전반적 평가 1-2문장]

## Critical (P0)
- file:line - issue
- file:line - issue

## Major (P1)
- file:line - issue

## Minor (P2)
- file:line - issue

## Nits
- file:line - suggestion

## Good Practices
- file:line - praise
```

## Priority Levels

- **P0 (Critical)**: 버그, 보안 이슈
- **P1 (Major)**: 성능, 유지보수성
- **P2 (Minor)**: 스타일, 컨벤션
- **Nits**: 선택적 개선사항

## Examples

```markdown
## Summary
전반적으로 좋은 구조. 2개 critical issues.

## Critical (P0)
- UserService.java:42 - SQL Injection 취약점
- AuthController.java:15 - 비밀번호 평문 저장

## Major (P1)
- UserRepository.java:28 - N+1 쿼리 문제

## Good Practices
- User.java:12 - Value Object 잘 활용
```
```

## 스타일 조합

여러 스타일을 조합할 수 있습니다:

```json
{
  "outputStyle": "concise-korean",
  "customInstructions": "코드 리뷰 시에는 'Code Review' 스타일 사용"
}
```

## 모범 사례

### DO
- 팀의 커뮤니케이션 선호도 반영
- 명확한 예시 포함
- 일관된 포맷 유지
- 경험 수준에 맞춤

### DON'T
- 너무 극단적인 스타일
- 중요한 정보 생략
- 프로젝트 코딩 규칙과 충돌
- 읽기 어려운 포맷

## 기본 Style 복원

기본 스타일로 되돌리려면:

```json
{
  "outputStyle": "Default"
}
```

또는:
```
"기본 스타일로 답해줘"
```

## 참고

- Style은 Claude의 응답 형식만 변경합니다
- 코딩 규칙이나 로직은 변경하지 않습니다
- 팀원과 합의된 스타일 사용을 권장합니다