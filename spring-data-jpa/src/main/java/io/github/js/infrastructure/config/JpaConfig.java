package io.github.js.infrastructure.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.js.infrastructure.persistence.article.ArticleRepositoryImpl;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
@EnableJpaRepositories(basePackages = {
        "io.github.js.domain",
        "io.github.js.infrastructure"
})
public class JpaConfig {

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }

    @Bean
    public ArticleRepositoryImpl articleRepositoryImpl(JPAQueryFactory queryFactory) {
        return new ArticleRepositoryImpl(queryFactory);
    }
}
