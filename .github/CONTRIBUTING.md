# Contributing to Spring Core

Spring Core 프로젝트에 기여해주셔서 감사합니다!

## 기여 방법

### 1. Issue 생성

버그 발견이나 기능 제안 시:

1. 기존 Issue 확인 (중복 방지)
2. 적절한 템플릿 선택:
   - Bug Report
   - Feature Request
   - Documentation Improvement
   - Tech Debt / Refactoring
3. 필수 정보 작성
4. 라벨 선택 (type, module, priority)

### 2. 개발 환경 설정

#### 필수 요구사항

**Java 11 모듈:**
- Java 11 JDK
- Gradle 7.x

**Java 17 모듈:**
- Java 17 JDK
- Gradle 8.x

#### 프로젝트 클론

```bash
# {YOUR_GITHUB_ORG}를 실제 GitHub 조직/사용자명으로 변경
git clone https://github.com/{YOUR_GITHUB_ORG}/spring-core.git
cd spring-core
```

#### Git Hooks 설치

```bash
./.githooks/install-hooks.sh
```

#### 모듈 빌드 및 테스트

```bash
cd spring-data-jpa
./gradlew build
./gradlew test
```

### 3. 브랜치 전략

#### 브랜치 네이밍

```
feature/모듈명-기능명     # 새 기능
fix/모듈명-버그명         # 버그 수정
docs/문서명              # 문서
refactor/모듈명-리팩터링명 # 리팩터링
test/모듈명-테스트명      # 테스트
```

**예시:**
```bash
git checkout -b feature/data-jpa-user-profile
git checkout -b fix/security-jwt-token-expire
git checkout -b docs/api-specification
```

### 4. 코드 작성 가이드

#### Coding Standards

모든 코드는 프로젝트 규칙을 따라야 합니다:
- [Java Style](.claude/rules/java-style.md)
- [Testing](.claude/rules/testing.md)
- [API Design](.claude/rules/api-design.md)
- [Security](.claude/rules/security.md)

#### 필수 체크리스트

- [ ] 코드가 컴파일됨
- [ ] 모든 테스트 통과
- [ ] 새 기능에 테스트 추가
- [ ] 문서 업데이트 (필요 시)
- [ ] 커밋 메시지 컨벤션 준수

### 5. 커밋 메시지 규칙

#### Format

```
<type>: <description>

[optional body]
```

#### Types

- `feat`: 새 기능
- `fix`: 버그 수정
- `docs`: 문서
- `test`: 테스트
- `refactor`: 리팩터링
- `style`: 코드 포맷팅
- `chore`: 기타
- `perf`: 성능 개선

#### Examples

```bash
feat: 유저 프로필 조회 API 추가

fix: 이메일 중복 검증 버그 수정

docs: REST API 문서 업데이트

test: UserService 단위 테스트 추가
```

### 6. Pull Request

#### PR 생성 전

1. **테스트 실행**
   ```bash
   ./gradlew test
   ```

2. **Rebase (if needed)**
   ```bash
   git fetch origin develop
   git rebase origin/develop
   ```

3. **Push**
   ```bash
   git push origin feature/data-jpa-user-profile
   ```

#### PR 생성

1. GitHub에서 "New Pull Request" 클릭
2. Base: `develop`, Compare: `your-branch`
3. PR 템플릿 작성:
   - Summary
   - Changes
   - Test Plan
   - Checklist
4. 라벨 추가 (type, module)
5. Reviewer 할당 (자동 할당됨)

#### PR 크기 가이드

- **적절**: 변경 파일 < 10개, 리뷰 시간 < 30분
- **큰 PR**: 작은 PR로 분할 권장

### 7. 코드 리뷰

#### For Author

- 리뷰 요청 후 24시간 내 응답 대기
- 피드백에 대해 답변 또는 수정
- "Request changes" 해결 후 re-request review

#### For Reviewer

- PR 생성 후 48시간 내 리뷰
- 건설적인 피드백 제공
- Approve / Request changes / Comment

### 8. Merge

#### 조건

- [ ] CI 통과
- [ ] 최소 1명 Approve
- [ ] 충돌 없음
- [ ] 모든 대화 해결됨

#### Merge 방법

- **Squash Merge** (권장): 히스토리 정리
- Merge Commit: 커밋 보존이 중요한 경우
- Rebase Merge: 선형 히스토리

### 9. 테스트 작성

#### 필수 테스트

- **Unit Tests**: 모든 public 메소드
- **Integration Tests**: 주요 API 엔드포인트
- **Exception Tests**: 예외 시나리오

#### Test Conventions

```java
@Test
void 유저_생성_테스트() {
    // given
    UserDto dto = new UserDto("user@example.com", "password123");

    // when
    User user = userService.createUser(dto.toEntity());

    // then
    assertThat(user.getEmail().getValue()).isEqualTo("user@example.com");
}
```

### 10. 문서화

#### 언제 문서 업데이트?

- 새 모듈 추가
- API 엔드포인트 변경
- 설정 파일 수정
- 아키텍처 변경

#### 문서 위치

- **모듈 README**: `{module}/README.md`
- **API 문서**: `spring-rest-docs/src/docs/asciidoc/`
- **전체 가이드**: `CLAUDE.md`

### 11. 보안

#### 절대 커밋 금지

- 비밀번호, API 키
- `.env` 파일
- 프로덕션 설정
- 개인 정보

#### 발견 시

1. 즉시 키 무효화
2. `.gitignore` 추가
3. Git history에서 제거:
   ```bash
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch path/to/secret" \
     --prune-empty --tag-name-filter cat -- --all
   ```

### 12. 라이센스

이 프로젝트에 기여함으로써 귀하의 기여가 프로젝트와 동일한 라이센스 하에 있음에 동의합니다.

### 13. 질문?

- GitHub Discussions 사용
- Issue에 `question` 라벨로 생성
- 팀 채널에서 문의

---

**감사합니다!** 🙏
