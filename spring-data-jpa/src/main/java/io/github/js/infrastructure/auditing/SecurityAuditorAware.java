package io.github.js.infrastructure.auditing;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * AuditorAware 구현체.
 * 실무에서는 SecurityContextHolder에서 인증 정보를 읽는다.
 * 이 모듈은 Spring Security 의존성이 없으므로 ThreadLocal 기반으로 대체한다.
 */
@Component("securityAuditorAware")
public class SecurityAuditorAware implements AuditorAware<String> {

    private static final ThreadLocal<String> CURRENT_AUDITOR = new ThreadLocal<>();

    public static void set(String auditor) {
        CURRENT_AUDITOR.set(auditor);
    }

    public static void clear() {
        CURRENT_AUDITOR.remove();
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(CURRENT_AUDITOR.get()).or(() -> Optional.of("system"));
    }
}
