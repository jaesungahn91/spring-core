---
name: pr-quality-check
description: PR 전 빌드 + 테스트를 일괄 실행하고 결과를 리포트한다. workflow 스킬 내부에서도 호출된다.
argument-hint: [module-name]
allowed-tools: Bash(./gradlew:*)
model: haiku
---

# Quality Check

## Steps

1. 인자로 모듈명이 주어지면 해당 모듈만, 없으면 모든 모듈 대상
2. `cd {module} && ./gradlew clean build -x test` 실행 (빌드)
3. `cd {module} && ./gradlew test` 실행 (테스트)
4. 완료 후 결과 테이블 출력: 모듈 | 빌드 | 테스트 PASS/FAIL | 테스트 수
5. 실패 항목 있으면 실패한 클래스명 + 에러 메시지만 요약 출력

## Modules

| 모듈 | Spring Boot | Java |
|------|-------------|------|
| spring-security-jwt | 2.7.x | 11 |
| spring-data-jpa | 2.7.x | 11 |
| spring-rest-docs | 2.7.x | 11 |
| spring-data-mongodb | 3.3.x | 17 |
| spring-batch | 3.5.x | 17 |

## Rules

- 빌드 실패 시 해당 모듈 테스트는 건너뜀
- 한 모듈 실패해도 나머지 모듈 계속 실행
- 전체 실패 없으면 "All checks passed" 출력
- 전체 스택트레이스 출력 금지, 실패 원인만 요약