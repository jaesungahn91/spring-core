package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.User;
import io.github.js.batch.job.BatchJobListener;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

/**
 * Partitioning 예제.
 *
 * 핵심 개념:
 * - Master Step: UserRangePartitioner가 User ID 범위를 gridSize개 파티션으로 분할
 * - Worker Step: 각 파티션이 독립적인 스레드에서 자신의 ID 범위만 처리
 * - stepExecutionContext의 minId/maxId를 기반으로 범위 쿼리 실행
 *
 * Multi-threaded Step과의 차이:
 * - Multi-threaded Step: 하나의 Reader를 synchronized로 직렬화하여 공유
 * - Partitioning: 각 Worker가 독립적인 Reader 인스턴스를 가짐 (lock 경합 없음)
 */
@Configuration
public class UserPartitionJobConfig {

    @Bean
    public Job userPartitionJob(JobRepository jobRepository,
                                Step userPartitionMasterStep,
                                BatchJobListener batchJobListener) {
        return new JobBuilder("userPartitionJob", jobRepository)
                .listener(batchJobListener)
                .start(userPartitionMasterStep)
                .build();
    }

    @Bean
    public Step userPartitionMasterStep(JobRepository jobRepository,
                                        UserRangePartitioner userRangePartitioner,
                                        Step userPartitionWorkerStep,
                                        TaskExecutor partitionTaskExecutor) {
        return new StepBuilder("userPartitionMasterStep", jobRepository)
                .partitioner("userPartitionWorkerStep", userRangePartitioner)
                .step(userPartitionWorkerStep)
                .gridSize(4)
                .taskExecutor(partitionTaskExecutor)
                .build();
    }

    @Bean
    public Step userPartitionWorkerStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        JpaCursorItemReader<User> userPartitionReader,
                                        JpaItemWriter<User> userPartitionWriter,
                                        BatchJobListener batchJobListener) {
        return new StepBuilder("userPartitionWorkerStep", jobRepository)
                .<User, User>chunk(10, transactionManager)
                .reader(userPartitionReader)
                .writer(userPartitionWriter)
                .listener(batchJobListener)
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<User> userPartitionReader(
            EntityManagerFactory entityManagerFactory,
            @Value("#{stepExecutionContext['minId']}") Long minId,
            @Value("#{stepExecutionContext['maxId']}") Long maxId) {
        return new JpaCursorItemReaderBuilder<User>()
                .name("userPartitionReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT u FROM User u WHERE u.id BETWEEN :minId AND :maxId ORDER BY u.id")
                .parameterValues(Map.of("minId", minId, "maxId", maxId))
                .build();
    }

    @Bean
    public JpaItemWriter<User> userPartitionWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<User>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public TaskExecutor partitionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("partition-");
        executor.initialize();
        return executor;
    }

}
