package io.github.js.batch.job.cleanup;

import io.github.js.batch.job.BatchJobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class FileCleanupJobConfig {

    @Bean
    public Job fileCleanupJob(JobRepository jobRepository,
                              @Qualifier("fileCleanupStep") Step fileCleanupStep,
                              BatchJobListener batchJobListener) {
        return new JobBuilder("fileCleanupJob", jobRepository)
                .listener(batchJobListener)
                .start(fileCleanupStep)
                .build();
    }

    @Bean
    public Step fileCleanupStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                FileCleanupTasklet fileCleanupTasklet,
                                BatchJobListener batchJobListener) {
        return new StepBuilder("fileCleanupStep", jobRepository)
                .tasklet(fileCleanupTasklet, transactionManager)
                .listener(batchJobListener)
                .build();
    }

    @Bean
    @StepScope
    public FileCleanupTasklet fileCleanupTasklet(
            @Value("#{jobParameters['targetDirectory']}") String targetDirectory,
            @Value("#{jobParameters['retentionDays'] ?: 7L}") long retentionDays) {
        return new FileCleanupTasklet(targetDirectory, retentionDays);
    }

}