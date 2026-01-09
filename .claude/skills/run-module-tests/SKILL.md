---
name: run-module-tests
description: Runs tests for Spring Boot modules with proper Java version handling. Use when testing code changes, debugging test failures, or verifying module functionality.
allowed-tools: Bash(gradle:*), Bash(export:*), Read
model: haiku
---

# Run Module Tests

Spring Boot 모듈의 테스트를 Java 버전에 맞춰 실행합니다.

## When to Use

- 코드 변경 후 테스트 확인
- 테스트 실패 디버깅
- PR 전 전체 테스트 실행
- 특정 기능 검증

## Module List

### Spring Boot 2.7.x (Java 11)
- `spring-security-jwt` - JWT 인증
- `spring-data-jpa` - JPA + Querydsl
- `spring-rest-docs` - REST API 문서화

### Spring Boot 3.3.x (Java 17)
- `spring-data-mongodb` - MongoDB 통합
- `spring-batch` - 배치 처리

## Single Module Test

### Basic Test
```bash
cd spring-data-jpa
./gradlew test
```

### Test with Stacktrace
```bash
cd spring-data-jpa
./gradlew test --stacktrace
```

### Clean Test
```bash
cd spring-data-jpa
./gradlew clean test
```

### Specific Test Class
```bash
cd spring-data-jpa
./gradlew test --tests "io.github.js.acceptance.user.UserAcceptanceTest"
```

### Specific Test Method
```bash
cd spring-data-jpa
./gradlew test --tests "io.github.js.acceptance.user.UserAcceptanceTest.유저_생성_테스트"
```

### Test with Logging
```bash
cd spring-data-jpa
./gradlew test --info
```

## All Modules Test

### Sequential Execution
```bash
# Java 11 modules
for module in spring-security-jwt spring-data-jpa spring-rest-docs; do
  echo "Testing $module..."
  cd $module
  ./gradlew test || echo "$module FAILED"
  cd ..
done

# Java 17 modules
for module in spring-data-mongodb spring-batch; do
  echo "Testing $module..."
  cd $module
  ./gradlew test || echo "$module FAILED"
  cd ..
done
```

### Parallel Execution (faster)
```bash
cd spring-data-jpa && ./gradlew test &
cd spring-security-jwt && ./gradlew test &
cd spring-rest-docs && ./gradlew test &
wait
```

## Test Reports

### Location
```
{module}/build/reports/tests/test/index.html
```

### Open Report (macOS)
```bash
open spring-data-jpa/build/reports/tests/test/index.html
```

### View Test Summary
```bash
cat spring-data-jpa/build/test-results/test/*.xml | grep -E "tests=|failures=|errors="
```

## Java Version Handling

### Check Current Java Version
```bash
java -version
```

### Switch Java Version (macOS)
```bash
# Java 11
export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Verify
java -version
```

### Project-specific Java (Gradle Toolchain)
```gradle
// build.gradle에 설정되어 있으면 자동
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
```

## Troubleshooting

### DatabaseCleanup Issues
**증상**: 테스트 간 데이터 간섭
**해결**:
```java
@BeforeEach
public void setUp() {
    databaseCleanup.execute();  // 확인
}
```

### H2 Database Issues
**증상**: SQL 호환성 문제
**해결**: `application-test.properties` 확인
```properties
spring.datasource.url=jdbc:h2:mem:test;MODE=MySQL
```

### Port Already in Use
**증상**: `RANDOM_PORT`가 이미 사용 중
**해결**:
```bash
# 포트 사용 프로세스 확인
lsof -i :8080

# 프로세스 종료
kill -9 [PID]
```

### Gradle Daemon Issues
**증상**: 빌드가 멈추거나 느림
**해결**:
```bash
./gradlew --stop
./gradlew clean test
```

### Out of Memory
**증상**: `OutOfMemoryError`
**해결**: `gradle.properties`에 추가
```properties
org.gradle.jvmargs=-Xmx2g -XX:MaxPermSize=512m
```

## Test Categories

### Unit Tests
```bash
# Domain logic tests
./gradlew test --tests "io.github.js.domain.*"
```

### Integration Tests
```bash
# Acceptance tests
./gradlew test --tests "io.github.js.acceptance.*"
```

### Controller Tests
```bash
# Application layer tests
./gradlew test --tests "io.github.js.application.*"
```

## Continuous Testing

### Watch Mode (재실행)
```bash
# 파일 변경 시 자동 재실행
./gradlew test --continuous
```

### Incremental Build
```bash
# 변경된 테스트만 실행
./gradlew test --rerun-tasks
```

## CI/CD Integration

### GitHub Actions
```yaml
- name: Run tests
  run: |
    cd spring-data-jpa
    ./gradlew test
```

### Test Coverage
```bash
# JaCoCo report 생성
./gradlew test jacocoTestReport

# 리포트 위치
open build/reports/jacoco/test/html/index.html
```

## Performance Tips

1. **Gradle daemon**: 항상 켜두기 (자동)
2. **Parallel execution**: `--parallel` 옵션
3. **Build cache**: `--build-cache` 옵션
4. **Incremental build**: 변경 파일만 빌드

## Quick Reference

```bash
# 가장 많이 사용하는 커맨드

# 1. 모듈 테스트
cd spring-data-jpa && ./gradlew test

# 2. 실패 시 상세 로그
./gradlew test --stacktrace --info

# 3. 특정 테스트만
./gradlew test --tests "*UserAcceptanceTest"

# 4. 클린 후 테스트
./gradlew clean test

# 5. 테스트 리포트 열기
open build/reports/tests/test/index.html
```

## Related Files

- `{module}/build.gradle` - 테스트 설정
- `{module}/src/test/` - 테스트 소스
- `{module}/src/test/resources/application-test.properties` - 테스트 환경 설정