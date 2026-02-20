---
name: build
description: Build a specific module or all modules
argument-hint: [module-name]
allowed-tools: Bash(gradle:*), Bash(ls:*)
---

# Build Module

모듈을 빌드합니다.

## Available Modules

!`ls -d spring-* 2>/dev/null | xargs -n1 basename`

## Your Task

$1 인자가 있을 때:
- 해당 모듈 디렉토리로 이동
- `./gradlew clean build` 실행

$1 인자가 없을 때:
- 모든 spring-* 모듈 순차 빌드

## Output

- 빌드 성공/실패
- 생성된 JAR 위치 (`build/libs/`)
- 실패 시 에러 로그