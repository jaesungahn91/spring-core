# Commands (⚠️ Deprecated)

> **중요: 이 기능은 Skills로 통합되었습니다.**
> Commands는 하위 호환성을 위해 계속 동작하지만, 새로운 커맨드는 `.claude/skills/`에 작성하세요.
> 자세한 내용은 [../skills/README.md](../skills/README.md)를 참고하세요.

## 마이그레이션 가이드

**기존**: `.claude/commands/my-command.md`
```yaml
---
description: My command description
argument-hint: [arg]
allowed-tools: Bash(*)
---
```

**신규**: `.claude/skills/my-command/SKILL.md`
```yaml
---
name: my-command
description: My command description
argument-hint: [arg]
allowed-tools: Bash(*)
---
```

주요 차이점:
1. `name:` 필드 추가 필수
2. 디렉토리 구조 변경 (skills/명령어명/SKILL.md)
3. 지원 파일 추가 가능 (templates, scripts 등)

---

## 개요 (Legacy)

Commands는 자주 사용하는 작업을 간단한 명령어로 실행할 수 있게 해줍니다. 각 command는 특정 작업을 위한 템플릿 역할을 합니다.

## 현재 정의된 Commands

| Command | 인자 | 설명 |
|---------|------|------|
| `build` | `[module-name]` | 모듈 빌드 |
| `test` | `[module-name]` | 모듈 테스트 실행 |
| `module-info` | `<module-name>` | 모듈 정보 조회 |
| `commit` | - | Git 커밋 생성 |

## 사용 방법

Claude에게 다음과 같이 요청하면 자동으로 해당 command를 실행합니다:

```
"spring-security-jwt 모듈 빌드해줘"
→ build 커맨드 실행

"모든 테스트 돌려줘"
→ test 커맨드 실행 (인자 없음)
```

## Command 정의 구조

```markdown
---
description: What this command does
argument-hint: [optional-arg]
allowed-tools: Bash(gradle:*), Read
---

# Command Name

명령어 설명

## Your Task

$1 인자가 있을 때:
- 수행할 작업 A

$1 인자가 없을 때:
- 수행할 작업 B

## Output

결과 형식 정의
```

## Frontmatter 필드

| 필드 | 필수 | 설명 | 예시 |
|------|------|------|------|
| `description` | ✅ | 명령어 설명 | `Build a specific module` |
| `argument-hint` | ❌ | 인자 힌트 | `[module-name]`, `<required-arg>` |
| `allowed-tools` | ❌ | 허용된 도구 | `Bash(gradle:*), Read` |

### Argument Hint 규칙
- `[optional]`: 선택적 인자
- `<required>`: 필수 인자
- `[arg1] [arg2]`: 여러 인자

## 동적 콘텐츠

Commands는 실행 시점의 정보를 포함할 수 있습니다:

```markdown
## Current Modules

Available modules:
!`ls -d spring-* 2>/dev/null | xargs -n1 basename`
```

`!` 로 시작하는 백틱 블록은 실행되어 결과가 삽입됩니다.

## Command 추가 예제

### 예제 1: 의존성 업데이트

```markdown
---
description: Update dependencies for a module
argument-hint: <module-name>
allowed-tools: Bash(./gradlew:*), Edit
---

# Update Dependencies

모듈의 의존성을 업데이트합니다.

## Available Modules

!`ls -d spring-* | xargs -n1 basename`

## Your Task

$1 모듈의:
1. `build.gradle` 읽기
2. 오래된 의존성 식별
3. 최신 버전으로 업데이트
4. `./gradlew $1:dependencies` 실행
5. 결과 요약

## Output

- 업데이트된 의존성 목록
- 이전 버전 → 새 버전
- 호환성 이슈 경고
```

### 예제 2: 로그 분석

```markdown
---
description: Analyze application logs for errors
argument-hint: [log-file]
allowed-tools: Bash(cat:*), Bash(grep:*), Read
---

# Analyze Logs

애플리케이션 로그를 분석합니다.

## Your Task

$1 파일 또는 기본 로그 위치에서:
1. ERROR 레벨 로그 추출
2. 발생 빈도 계산
3. 패턴 분석
4. 원인 추정

## Output

```markdown
## Error Summary
- [에러 타입]: 발생 횟수
- [에러 타입]: 발생 횟수

## Top 3 Errors
1. [에러 메시지] - 원인 추정
2. ...

## Recommendations
- 권장 조치
```
```

### 예제 3: API 문서 생성

```markdown
---
description: Generate REST API documentation
argument-hint: [module-name]
allowed-tools: Bash(./gradlew:*), Read(build/docs/**), Write(*.md)
---

# Generate API Docs

REST API 문서를 생성합니다.

## Your Task

$1 모듈 또는 전체에 대해:
1. Spring REST Docs 빌드 실행
2. 생성된 HTML 확인
3. Markdown 요약 생성
4. API 엔드포인트 목록 작성

## Output Format

```markdown
# API Documentation

## Endpoints

### User API
- `GET /users` - 유저 목록
- `POST /users` - 유저 생성
...

## Request/Response Examples
[자동 생성된 예제]
```
```

## 모범 사례

### DO
- 한 가지 작업에 집중
- 명확한 인자 정의
- 일관된 출력 포맷
- 에러 케이스 처리

### DON'T
- 너무 복잡한 로직
- 부작용이 큰 작업 (배포 등)
- 모호한 동작
- 도구 제한 없이 모든 권한

## 인자 사용

Command는 `$1`, `$2` 등으로 인자에 접근합니다:

```markdown
$1 모듈의 테스트 실행:
- `cd $1`
- `./gradlew test`

$1이 없으면:
- 모든 모듈 순회
```

## 디버깅

Command가 예상대로 작동하지 않으면:

1. **인자 확인**: `argument-hint`가 명확한가?
2. **도구 권한**: `allowed-tools`에 필요한 도구가 있는가?
3. **동적 콘텐츠**: `!` 명령어가 올바르게 실행되는가?

## Commands vs Agents

| 측면 | Commands | Agents |
|------|----------|--------|
| 목적 | 간단한 작업 자동화 | 복잡한 워크플로우 |
| 권한 | 제한적 | 더 많은 도구 접근 |
| 인자 | 위치 기반 (`$1`, `$2`) | 컨텍스트 기반 |
| 실행 | 즉시 | 다단계 프로세스 |

## 참고

- Command는 일회성 작업에 적합합니다
- 복잡한 로직은 Agent로 분리하세요
- 자주 사용하는 작업만 Command로 만드세요