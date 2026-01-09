---
paths: "*/src/**/*.java"
---

# Java Code Style Rules

## Naming Conventions

### Classes
- **Entities**: 명사형 (`User`, `Article`)
- **Services**: `*Service` (`UserService`)
- **Controllers**: `*Controller` (`UserController`)
- **DTOs**: `*Dto` (`UserDto`)
- **Repositories**: `*Repository` (`UserRepository`)

### Methods
- **생성**: `create*()`
- **조회**: `get*()` 또는 `find*()`
- **수정**: `update*()`
- **삭제**: `delete*()`

### Variables
- camelCase: `userId`, `emailAddress`
- 상수: UPPER_SNAKE_CASE: `MAX_LENGTH`

## Code Structure

### Class Organization
1. Static fields
2. Instance fields
3. Constructors
4. Public methods
5. Private methods

### Method Length
- 30줄 이하 권장
- 초과 시 private 헬퍼 메소드로 분리

## Comments
- 복잡한 비즈니스 로직만 주석
- 자명한 코드는 주석 생략