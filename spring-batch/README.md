![spring](https://img.shields.io/badge/Spring%20boot-3.5.x-green)
![java](https://img.shields.io/badge/Java-17-red)

# Spring Batch

## 개요

Spring Batch의 두 가지 처리 패턴(Chunk, Tasklet)을 비교 시연하는 예제 모듈이다.

| Job | 방식 | 목적 |
|-----|------|------|
| `UserImportJob` | Chunk | CSV → 유효성 검사 → DB 저장 |
| `FileCleanupJob` | Tasklet | 지정 디렉토리에서 오래된 파일 삭제 |

---

## 모듈 구조

```
src/main/java/io/github/js/batch/
├── domain/user/          # JPA 엔티티 및 Repository
├── job/
│   ├── cleanup/          # FileCleanupJob (Tasklet 기반)
│   └── user/             # UserImportJob (Chunk 기반)
└── scheduler/            # Spring Scheduler 기반 Job 트리거
```

---

## Jobs

### UserImportJob

**처리 흐름**

```
[users.csv]
  → FlatFileItemReader<UserCsvDto>    # CSV 파싱
  → UserItemProcessor                 # 이메일·나이 유효성 검사, User 변환
  → JpaItemWriter<User>               # DB 저장
```

**처리 규칙**

- 유효 이메일 형식(`@` 포함) 통과
- 나이 1–150 범위 통과
- 그 외: processor에서 `null` 반환 → filter

**주요 설정**

- Chunk size: 10
- Skip: `FlatFileParseException` (최대 5건)
- `spring.batch.job.enabled=false` → 자동 실행 비활성화, 스케줄러로 제어

**Listeners 역할 분리**

| Listener | 시점 | 출력 내용 |
|----------|------|----------|
| `UserImportStepListener` | Step 완료 직후 | read / write / filter / skip 상세 카운트 |
| `UserImportJobListener` | Job 완료 후 | 전체 소요 시간, 총 처리 건수, 최종 상태 (COMPLETED / FAILED 공통) |

**Job Parameters**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `inputFile` | String | 입력 파일 경로 (`classpath:` 또는 절대경로 모두 지원) |

---

### FileCleanupJob

**처리 흐름**

```
[targetDirectory]
  → FileCleanupTasklet
      → 디렉토리 존재 확인
      → retentionDays 이전에 수정된 파일 탐색
      → 파일 삭제 (deleteCount writeCount 기록)
```

**Job Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| `targetDirectory` | String | (필수) | 정리 대상 디렉토리 경로 |
| `retentionDays` | Long | 7 | 파일 보존 기간(일) |

---

## 스케줄링

| Job | Cron | 실행 시각 |
|-----|------|----------|
| `UserImportJob` | `0 0 2 * * ?` | 매일 새벽 2시 |
| `FileCleanupJob` | `0 0 3 * * ?` | 매일 새벽 3시 |

`spring.batch.job.enabled=false`로 애플리케이션 시작 시 Job 자동 실행을 막고, `BatchScheduler`가 cron 표현식에 따라 Job을 트리거한다. 이 구조를 통해 Job 실행 시점을 코드 변경 없이 스케줄러 설정만으로 제어할 수 있다.

---

## 아키텍처 결정 사항

### 1. Listener 역할 분리

- **결정**: StepListener는 Step 즉각 피드백, JobListener는 Job 수준 집계 요약만 담당
- **배경**: 초기 구현에서 두 Listener가 동일한 read/write/skip 카운트를 중복 출력함. FAILED 시 JobListener가 침묵함
- **근거**: Step과 Job은 관심사가 다르다. Step은 "이 Step에서 무슨 일이 있었나", Job은 "전체 실행이 어땠나". 중복 없이 계층별 정보를 분리해야 로그가 명확하다

### 2. Listener Bean 관리 일원화

- **결정**: `UserImportStepListener`와 `UserImportJobListener` 모두 `@Bean`으로 config에서 선언, `@Component` 제거
- **배경**: `UserImportJobListener`는 `@Component`, `UserImportStepListener`는 `new`로 직접 생성하는 비일관 구조였음
- **근거**: 같은 설정 클래스에서 일부는 IoC 컨테이너 관리, 일부는 직접 생성하는 혼용은 설계 냄새다. 추후 의존성 주입이 필요해질 때 `new` 방식은 변경 비용이 크다

### 3. retentionDays Job Parameter화

- **결정**: `retentionDays`를 Job Parameter로 외부 주입. 기본값 7 (SpEL Elvis operator 활용)
- **배경**: `targetDirectory`는 Job Parameter이고 `retentionDays`는 코드에 하드코딩. 같은 생성자 인자가 서로 다른 공급 방식을 가짐
- **근거**: "무엇이 변할 수 있는가"에 대한 의도가 코드에 드러나야 한다. 같은 맥락의 두 값이 다르게 공급되면 읽는 사람이 그 이유를 유추해야 한다. 운영 환경에서 보존 기간을 재배포 없이 조정할 수 있다는 실용적 이점도 있다

### 4. FileCleanupJob 스케줄 등록

- **결정**: `BatchScheduler`에 `fileCleanupJob` 스케줄 추가 (매일 새벽 3시)
- **배경**: `userImportJob`만 스케줄이 등록돼 있었음. 모듈이 두 Job 패턴을 함께 시연하는 목적이라면 미완성
- **근거**: 예제 모듈의 완성도 측면. 두 Job이 모두 동작 가능한 상태여야 패턴 비교가 의미 있다

### 5. ResourceLoader를 통한 입력 파일 경로 추상화

- **결정**: `ClassPathResource` 대신 `ResourceLoader.getResource()`로 교체
- **배경**: `inputFile`이 Job Parameter로 외부 주입되는 동적 값임에도 `ClassPathResource`로 고정해 classpath 내부 파일만 허용
- **근거**: 운영 환경에서 배치 입력 파일은 외부 경로에 위치한다. classpath는 빌드 산출물이므로 재배포 없이 교체가 불가능하다. `ResourceLoader`는 `classpath:` prefix와 절대경로를 모두 처리하므로 테스트(classpath)와 운영(외부 경로)을 동일한 코드로 지원한다

---

## 설정

| 항목 | 값 | 설명 |
|------|-----|------|
| `spring.datasource.url` | `jdbc:h2:mem:batchdb` | H2 인메모리 DB (Spring Batch 메타데이터 저장) |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | 애플리케이션 시작/종료 시 스키마 재생성 |
| `spring.batch.job.enabled` | `false` | 애플리케이션 시작 시 Job 자동 실행 비활성화 |
| `spring.batch.jdbc.initialize-schema` | `always` | Batch 메타데이터 테이블 자동 생성 |

---

## 테스트

**UserImportJobTest**

- `userImportJob_completedSuccessfully`: Job 상태 `COMPLETED` 확인
- `userImportJob_savesOnlyValidUsers`: 유효 유저 4건만 저장 검증
- `userImportJob_stepExecution_countsCorrect`: read:6 / write:4 / filter:2 카운트 검증

**FileCleanupJobTest**

- `fileCleanupJob_completedSuccessfully`: Job 상태 `COMPLETED` 확인
- `fileCleanupJob_deletesOldFiles`: 8일 전 파일만 삭제, 최신 파일 보존 검증

테스트 실행:

```bash
./gradlew :spring-batch:test
```

---

## 참고

- https://spring.io/projects/spring-batch
- https://docs.spring.io/spring-batch/reference