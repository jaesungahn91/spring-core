---
name: build
description: Build a specific module or all modules. Use when the user wants to compile, build, or package a module.
argument-hint: [module-name]
allowed-tools: Bash(./gradlew:*)
---

# Build Module

## Steps

1. $1 인자가 있으면 해당 모듈만, 없으면 모든 `spring-*` 모듈 순차 빌드
2. 각 모듈: `cd {module} && ./gradlew clean build`
3. 결과 보고: 성공/실패, 실패 시 핵심 에러만 요약

## Rules

- 빌드 실패 시 전체 로그 대신 핵심 오류만 출력
- 모든 모듈 빌드 시 한 모듈 실패해도 나머지 계속 진행
- 생성된 JAR 위치: `{module}/build/libs/`