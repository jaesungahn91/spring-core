package io.github.js.acceptance;

import com.google.common.base.CaseFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@ActiveProfiles("test")
public class DatabaseCleanup implements InitializingBean {

    @PersistenceContext
    private EntityManager entityManager;

    private List<String> tableNames;

    /**
     * ID 컬럼이 없는 테이블: TRUNCATE는 실행하되 ALTER COLUMN ID RESTART 스킵
     * - article_tags: @ManyToMany 조인 테이블 (article_id, tag_id만 존재)
     * - follows: @EmbeddedId 복합 PK 테이블 (follower_id, followee_id만 존재)
     */
    private static final Set<String> NO_IDENTITY_TABLES = Set.of("article_tags", "follows");

    /**
     * 엔티티명 → 실제 테이블명 매핑 (H2 예약어 충돌 등 특수 케이스)
     */
    private static final java.util.Map<String, String> TABLE_NAME_OVERRIDES =
            java.util.Map.of(
                    "user", "users",
                    "article", "articles",
                    "tag", "tags",
                    "comment", "comments",
                    "follow", "follows"
            );

    @Override
    public void afterPropertiesSet() {
        tableNames = entityManager.getMetamodel()
                .getEntities()
                .stream()
                .filter(e -> e.getJavaType().getAnnotation(Entity.class) != null)
                .map(e -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, e.getName()))
                .collect(Collectors.toList());

        // @ManyToMany 조인 테이블은 엔티티 메타모델에 포함되지 않으므로 수동 추가
        tableNames.add("article_tags");
    }

    @Transactional
    public void execute() {
        entityManager.flush();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        for (String entityTableName : tableNames) {
            String tableName = TABLE_NAME_OVERRIDES.getOrDefault(entityTableName, entityTableName);
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();

            if (!NO_IDENTITY_TABLES.contains(tableName)) {
                entityManager.createNativeQuery(
                        "ALTER TABLE " + tableName + " ALTER COLUMN ID RESTART WITH 1"
                ).executeUpdate();
            }
        }

        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }
}
