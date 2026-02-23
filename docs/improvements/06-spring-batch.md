# spring-batch 모듈 개선안

## 모듈 개요

### 목적
Spring Batch를 활용한 배치 처리 시스템 학습

### 현재 구현 수준
- **Main 클래스**: ✅ 있음
- **Job**: ❌ 없음
- **Step**: ❌ 없음
- **Reader/Processor/Writer**: ❌ 없음
- **스케줄링**: ❌ 없음
- **테스트**: ❌ 전무

### 완성도
**5%** - 프로젝트 구조만 존재, 구현 전무

## 현재 상태 분석

### 구현된 것
**파일**: `spring-batch/src/main/java/io/github/js/batch/SpringBatchApplication.java`

- Spring Boot Main 클래스만 존재
- 실행 가능하지만 배치 Job이 없음

### 누락된 기능
- Job, Step 정의
- ItemReader / ItemProcessor / ItemWriter 구현
- 배치 메타데이터 DB 설정
- 스케줄링 설정
- 재시도/Skip 로직
- 리스너 (JobExecutionListener, StepExecutionListener)
- 테스트

---

## 개선 항목

### P0 (필수)

#### 1. Chunk 기반 Job 구현 (CSV → DB)

**시나리오**: CSV 파일에서 사용자 데이터를 읽어 데이터베이스에 저장

**데이터 모델**:
- CSV: `email, name, age`
- DB: `users` 테이블

**파일**:
- `spring-batch/src/main/java/io/github/js/domain/user/User.java`
- `spring-batch/src/main/java/io/github/js/batch/job/user/UserCsvReader.java`
- `spring-batch/src/main/java/io/github/js/batch/job/user/UserItemProcessor.java`
- `spring-batch/src/main/java/io/github/js/batch/job/user/UserItemWriter.java`
- `spring-batch/src/main/java/io/github/js/batch/job/user/UserImportJobConfig.java`
- `spring-batch/src/main/resources/data/users.csv`

구현 내용:
- `FlatFileItemReader`: CSV 헤더 skip, `UserCsvDto`로 매핑
- `UserItemProcessor`: 유효하지 않은 이메일/나이 필터링, DTO → Entity 변환
- `JpaItemWriter` 또는 `RepositoryItemWriter`: DB 저장
- `JobExecutionListener`: Job 시작/종료 로그
- `StepExecutionListener`: Step 완료 후 read/write/skip 카운트 로그
- chunk size: 10
- faultTolerant + skip 설정 (skipLimit: 5)

**application.yml 설정**:
- `spring.batch.job.enabled=false` (자동 실행 방지)
- `spring.batch.jdbc.initialize-schema=always`

---

#### 2. Tasklet 기반 Job 구현 (파일 정리)

**시나리오**: 7일 이상 지난 임시 파일 삭제

**파일**: `spring-batch/src/main/java/io/github/js/batch/job/cleanup/FileCleanupJobConfig.java`

구현 내용:
- `FileCleanupJob`: 단일 Tasklet Step으로 구성
- Tasklet: `temp/` 디렉토리에서 7일 이상 지난 파일 삭제
- 삭제된 파일 수 로그

---

#### 3. 배치 테스트 작성

**파일**:
- `spring-batch/src/test/java/io/github/js/batch/job/user/UserImportJobTest.java`
- `spring-batch/src/test/java/io/github/js/batch/job/cleanup/FileCleanupJobTest.java`

**UserImportJobTest 케이스** (`@SpringBatchTest` 활용):
- Job 실행 성공 (BatchStatus.COMPLETED)
- 유효한 데이터만 저장됨 (잘못된 이메일/나이 필터링 확인)
- Step 단위 실행: readCount, writeCount, skipCount 검증

**FileCleanupJobTest 케이스**:
- Tasklet 실행 성공

---

### P1 (중요)

#### 4. Job Parameters 활용

- `@StepScope` + `@Value("#{jobParameters['inputFile']}")`: 동적 파일 경로
- 실행 시 파라미터로 CSV 파일 경로 지정 가능

---

#### 5. 재시도 및 Skip 전략

- `retry(TransientDataAccessException.class).retryLimit(3)`: DB 일시 오류 재시도
- `skip(FlatFileParseException.class).skipLimit(10)`: 파싱 오류 데이터 스킵

---

#### 6. 스케줄링 설정

**파일**: `spring-batch/src/main/java/io/github/js/batch/scheduler/BatchScheduler.java`

- `@Scheduled(cron = "0 0 2 * * ?")`: 매일 새벽 2시 실행
- `JobParameters`에 현재 시각 포함 (중복 실행 방지)
- Main 클래스에 `@EnableScheduling` 추가

---

### P2 (권장)

#### 7. 배치 모니터링 API

**파일**: `spring-batch/src/main/java/io/github/js/batch/api/BatchMonitoringController.java`

- `GET /api/batch/jobs`: 등록된 Job 이름 목록
- `GET /api/batch/jobs/{jobName}/executions`: Job 실행 이력 (status, startTime, endTime)
- `JobExplorer` 활용

---

## 변경 항목

### 1. Job 자동 실행 방지

- `application.yml`에 `spring.batch.job.enabled=false` 추가
- CommandLineRunner 또는 스케줄러로 제어

### 2. 트랜잭션 크기 설정

- Chunk size: 너무 크면 메모리 부족, 너무 작으면 성능 저하
- 일반적 권장: 10~100 (데이터 크기에 따라 조정)

---

## 권장 작업 순서

1. Entity, Repository 정의 + `application.yml` 설정
2. `FlatFileItemReader`, `UserItemProcessor`, `JpaItemWriter` 작성
3. `UserImportJobConfig`: Job, Step, 리스너 정의
4. `UserImportJobTest` 작성 + `FileCleanupJob` 구현 + `FileCleanupJobTest`
5. Job Parameters 활용 + 재시도/Skip 전략 적용
6. `BatchScheduler` 작성
7. (선택) 배치 모니터링 API 구현

---

## 참고 문서

- [Spring Batch Reference](https://docs.spring.io/spring-batch/docs/current/reference/html/)
- [Spring Batch Samples](https://github.com/spring-projects/spring-batch/tree/main/spring-batch-samples)

### 관련 파일
- Main: `spring-batch/src/main/java/io/github/js/batch/SpringBatchApplication.java`
- Config: `spring-batch/src/main/resources/application.yml`

### 학습 포인트
1. **Chunk 기반 처리**: 대용량 데이터 처리 패턴
2. **Tasklet**: 단순 작업 처리
3. **메타데이터 테이블**: 배치 실행 이력 관리
4. **재시도/Skip**: 장애 허용 전략
5. **Job Parameters**: 동적 파라미터 처리