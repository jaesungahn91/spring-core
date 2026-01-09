# Rules

Claude가 코드 작성 시 참고하는 프로젝트 규칙 모음입니다.

## 개요

Rules는 특정 파일 패턴에 대해 적용할 코딩 규칙을 정의합니다. Claude는 해당 패턴과 매칭되는 파일을 읽거나 수정할 때 자동으로 이 규칙들을 참고합니다.

## 작동 원리

각 rule 파일은 frontmatter에 `paths` 패턴을 정의합니다:

```markdown
---
paths: "*/src/**/*.java"
---

# Your rules here
```

Claude가 `paths` 패턴과 매칭되는 파일을 다룰 때 해당 rule이 컨텍스트에 자동으로 포함됩니다.

## 현재 정의된 Rules

| Rule | 적용 대상 | 설명 |
|------|----------|------|
| `workflow.md` | `**/*.java` | Issue-Driven Workflow (Issue 번호 필수) |
| `api-design.md` | `*/application/**/*.java` | REST API 설계 규칙 |
| `java-style.md` | `*/src/**/*.java` | Java 코드 스타일 |
| `testing.md` | `*/test/**/*.java` | 테스트 작성 규칙 |
| `database.md` | `*/repository/**/*.java`, `*/domain/**/*.java` | DB 접근 및 엔티티 규칙 |
| `exceptions.md` | `**/*.java` | 예외 처리 규칙 |
| `configuration.md` | `*/config/**/*.java` | Spring 설정 규칙 |
| `logging.md` | `**/*.java` | 로깅 규칙 |
| `security.md` | `*/security/**/*.java` | 보안 규칙 |

## Rule 추가 방법

### 1. 새 rule 파일 생성

```bash
touch .claude/rules/my-custom-rule.md
```

### 2. Frontmatter 작성

```markdown
---
paths: "*/src/main/java/io/github/js/service/**/*.java"
---

# My Custom Rule

서비스 레이어 작성 규칙입니다.

## Rules
- 트랜잭션 경계 명확히 설정
- 비즈니스 로직은 도메인 객체에 위임
...
```

### 3. 자동 적용 확인

이제 `io.github.js.service` 패키지의 파일을 다룰 때 Claude가 이 규칙을 참고합니다.

## Rule 작성 가이드

### 구조
- **명확한 제목**: 무엇에 대한 규칙인지
- **간결한 설명**: 불릿 포인트 위주
- **코드 예제**: 필요한 경우에만 최소한으로

### 예제
```markdown
---
paths: "*/repository/**/*.java"
---

# Repository Rules

## Naming
- 인터페이스명: `*Repository` (예: `UserRepository`)
- 메소드명: 쿼리 의도 명확히 (`findActiveUsersByEmail`)

## Query Methods
- 복잡한 쿼리는 `@Query` 사용
- Native query는 최소화

## Don'ts
- Repository에서 비즈니스 로직 구현 금지
- 트랜잭션 어노테이션 Repository에 사용 금지 (Service에서 관리)
```

## 주의사항

- Rule은 **가이드라인**이지 강제가 아닙니다
- 너무 상세하면 오히려 혼란을 줄 수 있습니다
- 프로젝트 진화에 따라 주기적으로 업데이트하세요
- 팀원과 합의된 내용만 추가하세요

## 비활성화

특정 rule을 임시로 비활성화하려면:

1. 파일명 변경: `my-rule.md.disabled`
2. 또는 frontmatter 제거

## 참고

- Rules는 로컬 `CLAUDE.md`와 글로벌 `~/.claude/CLAUDE.md`를 보완합니다
- 우선순위: 로컬 CLAUDE.md > rules > 글로벌 CLAUDE.md