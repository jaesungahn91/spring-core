package io.github.js.application;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 글로벌 예외 처리 — ProblemDetail (RFC 9457) 기반 에러 응답.
 *
 * Spring 6(Boot 3.x)에서 제공하는 ProblemDetail은 별도 에러 DTO 없이
 * type, title, status, detail, instance 구조의 표준 에러 응답을 생성한다.
 *
 * 응답 예시:
 * {
 *   "type": "about:blank",
 *   "title": "Not Found",
 *   "status": 404,
 *   "detail": "Article not found: 99"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        return problem;
    }

    /**
     * 낙관적 잠금 충돌: 동시 수정 시 나중에 flush하는 쪽에서 발생
     * 클라이언트는 최신 데이터를 다시 조회 후 재시도해야 한다.
     */
    @ExceptionHandler(OptimisticLockException.class)
    public ProblemDetail handleOptimisticLock(OptimisticLockException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "Resource was modified by another transaction. Please retry.");
        problem.setTitle("Optimistic Lock Conflict");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, detail);
        problem.setTitle("Validation Failed");
        return problem;
    }
}
