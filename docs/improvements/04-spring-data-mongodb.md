# spring-data-mongodb 모듈 개선안

## 모듈 개요

### 목적
Spring Data MongoDB를 활용한 NoSQL 데이터베이스 학습

### 현재 구현 수준
- **Repository**: ✅ MongoRepository 사용
- **Entity**: ✅ Item 엔티티 정의
- **REST API**: ❌ Controller, Service 없음
- **인덱스**: ❌ 미적용
- **Custom Repository**: ❌ 미구현
- **테스트**: ⚠️ 부족 (추정 30-35%)

### 완성도
**30%** - Repository만 구성, REST API 레이어 전무

## 현재 상태 분석

### 구현된 기능

#### 1. Entity 설계
**파일**: `spring-data-mongodb/src/main/java/io/github/js/domain/item/Item.java`

- `@Document` 매핑, 기본 필드 정의: 구현됨
- 인덱스 설정 없음, 검증 로직 부족, Auditing 미적용

#### 2. Repository
**파일**: `spring-data-mongodb/src/main/java/io/github/js/domain/item/ItemRepository.java`

- `MongoRepository` 기본 CRUD 가능
- 커스텀 쿼리 메서드 없음, 집계 파이프라인 예제 없음

### 누락된 기능
- REST API 레이어 (Controller, Service)
- 인덱스 활용
- Custom Repository (MongoTemplate 활용)
- 집계 파이프라인
- 텍스트 검색
- 페이징/정렬

---

## 개선 항목

### P0 (필수)

#### 1. REST API 레이어 구현

**파일**:
- `spring-data-mongodb/src/main/java/io/github/js/application/item/ItemService.java`
- `spring-data-mongodb/src/main/java/io/github/js/application/item/ItemRestController.java`
- `spring-data-mongodb/src/main/java/io/github/js/application/item/dto/`

구현 내용:
- `ItemService`: 생성, 단건 조회, 목록 조회, 수정, 삭제
- `ItemRestController`: CRUD 엔드포인트 (`/api/items`)
- DTO: `CreateItemRequest` (name, description, price, quantity, category, tags), `UpdateItemRequest`, `ItemResponse`
- 입력 검증: `@NotBlank`, `@NotNull`, `@Min`

---

#### 2. Exception 처리

**파일**: `spring-data-mongodb/src/main/java/io/github/js/domain/item/exception/`

- `ItemNotFoundException`: errorCode `ITEM_NOT_FOUND`
- `DuplicateItemNameException`: errorCode `DUPLICATE_ITEM_NAME`
- `InsufficientStockException`: 재고 부족 시 사용

---

#### 3. 테스트 작성

**파일**:
- `spring-data-mongodb/src/test/java/io/github/js/application/item/ItemServiceTest.java`
- `spring-data-mongodb/src/test/java/io/github/js/application/item/ItemRestControllerTest.java`

**ItemServiceTest 케이스**:
- 상품 생성 성공
- 중복 이름으로 생성 실패 (`DuplicateItemNameException`)
- 상품 조회 성공
- 존재하지 않는 상품 조회 실패 (`ItemNotFoundException`)
- 상품 수정 성공
- 상품 삭제 성공

**ItemRestControllerTest 케이스**:
- `POST /api/items` 성공 (201 Created, Location 헤더)
- `POST /api/items` 잘못된 입력 (400)
- `GET /api/items/{id}` 성공 (200)
- `GET /api/items/{id}` 존재하지 않음 (404)
- `PUT /api/items/{id}` 수정 성공 (204)
- `DELETE /api/items/{id}` 삭제 성공 (204)

---

### P1 (중요)

#### 4. 인덱스 설정

**파일**: `spring-data-mongodb/src/main/java/io/github/js/domain/item/Item.java`

적용 내용:
- `name` 필드: `@Indexed(unique = true)`
- `category` 필드: `@Indexed`
- 복합 인덱스: `category + price` (조회 최적화)
- Auditing: `@CreatedDate`, `@LastModifiedDate`

**파일**: `spring-data-mongodb/src/main/java/io/github/js/infrastructure/config/MongoConfig.java`

- `@EnableMongoAuditing` 설정

---

#### 5. Custom Repository 구현

**파일**:
- `spring-data-mongodb/src/main/java/io/github/js/domain/item/ItemRepositoryCustom.java`
- `spring-data-mongodb/src/main/java/io/github/js/domain/item/ItemRepositoryImpl.java`

구현 내용:
- `ItemRepositoryCustom`: `searchItems(condition)`, `searchItemsWithPaging(condition, pageable)`, `getItemStatisticsByCategory()`
- `ItemRepositoryImpl`: `MongoTemplate` 기반 동적 쿼리 (name, category, minPrice, maxPrice, tags 필터)
- 집계 파이프라인: 카테고리별 상품 수, 평균 가격, 총 가치 집계

---

#### 6. 검색 API 구현

- `GET /api/items/search`: name, category, minPrice, maxPrice, tags 필터 + 페이징
- `GET /api/items/statistics`: 카테고리별 통계

---

### P2 (권장)

#### 7. 텍스트 검색 구현

- `name`, `description` 필드에 텍스트 인덱스 추가
- `TextQuery`를 활용한 Full-Text Search API 구현

---

#### 8. 트랜잭션 지원

- `MongoTransactionManager` 빈 설정
- 재고 이동 시나리오로 트랜잭션 예제 구현 (원자적 감소/증가)

---

## 변경 항목

### 1. Item Entity 검증 로직 추가

- 도메인 규칙 검증 없음 → `update()`, `decreaseStock()`, `increaseStock()` 메서드에 검증 추가

### 2. Repository 메서드명 개선

- `findByCategory` → `findAllByCategory`로 명확히 변경

---

## 신규 추가 항목

### 1. 통합 테스트 환경

**파일**: `spring-data-mongodb/src/test/java/io/github/js/integration/MongoIntegrationTestBase.java`

- `@Testcontainers` + MongoDB 컨테이너 설정
- `@DynamicPropertySource`로 MongoDB URI 주입
- `@BeforeEach`에서 컬렉션 초기화

### 2. 데이터 초기화

**파일**: `spring-data-mongodb/src/main/java/io/github/js/infrastructure/init/DataInitializer.java`

- `@Profile("local")` + `CommandLineRunner`
- 최초 실행 시 샘플 Item 데이터 삽입

---

## 권장 작업 순서

1. `ItemService`, `ItemRestController`, DTO 작성 + Exception 처리
2. `ItemServiceTest`, `ItemRestControllerTest` 작성 + 통합 테스트 환경 구축 → 커버리지 70%
3. Item Entity 인덱스 추가 + `MongoConfig` Auditing 설정
4. `ItemRepositoryCustom`, `ItemRepositoryImpl` 작성 + 검색 API 추가
5. (선택) 텍스트 검색, 집계 파이프라인, 트랜잭션 예제

---

## 참고 문서

- [Spring Data MongoDB Reference](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
- [MongoDB Manual](https://www.mongodb.com/docs/manual/)
- [MongoDB Aggregation](https://www.mongodb.com/docs/manual/aggregation/)

### 관련 파일
- Entity: `spring-data-mongodb/src/main/java/io/github/js/domain/item/Item.java`
- Repository: `spring-data-mongodb/src/main/java/io/github/js/domain/item/ItemRepository.java`

### 학습 포인트
1. **Document 모델**: MongoDB 도큐먼트 구조 설계
2. **인덱스**: 쿼리 성능 최적화
3. **Custom Repository**: MongoTemplate 활용
4. **집계 파이프라인**: 복잡한 데이터 분석
5. **트랜잭션**: MongoDB 4.0+ 트랜잭션 지원