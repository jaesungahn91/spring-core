---
name: run-module-tests
description: Runs tests for Spring Boot modules. Use when testing code changes, debugging test failures, or verifying functionality before a commit or PR.
argument-hint: [module-name]
allowed-tools: Bash(./gradlew:*)
model: haiku
---

# Run Module Tests

## Steps

1. 인자로 모듈명이 주어지면 해당 모듈만, 없으면 모든 모듈 순차 실행
2. `cd {module} && ./gradlew test` 실행
3. 실패 시: 실패한 테스트 클래스명 + 원인 메시지만 요약 출력
4. 완료 후: 모듈별 PASS/FAIL + 테스트 수 + 소요 시간 표로 출력

## Modules

| 모듈 | Spring Boot | Java |
|------|-------------|------|
| spring-security-jwt | 2.7.x | 11 |
| spring-data-jpa | 2.7.x | 11 |
| spring-rest-docs | 2.7.x | 11 |
| spring-data-mongodb | 3.3.x | 17 |
| spring-batch | 3.5.x | 17 |

## Rules

- 테스트 실패 시 전체 스택트레이스 대신 실패 원인만 출력
- 한 모듈 실패해도 나머지 모듈 계속 실행
- Gradle toolchain 설정이 있으므로 Java 버전 수동 전환 불필요
