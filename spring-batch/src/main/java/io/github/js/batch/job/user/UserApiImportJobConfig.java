package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.User;
import io.github.js.batch.job.BatchJobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class UserApiImportJobConfig {

    @Bean
    @StepScope // 상태(currentPage, buffer)를 가지므로 Step 실행마다 새 인스턴스 필요
    public UserApiItemReader userApiItemReader() {
        return new UserApiItemReader();
    }

    @Bean
    public Job userApiImportJob(JobRepository jobRepository,
                                @Qualifier("userApiImportStep") Step userApiImportStep,
                                BatchJobListener batchJobListener) {
        return new JobBuilder("userApiImportJob", jobRepository)
                .listener(batchJobListener)
                .start(userApiImportStep)
                .build();
    }

    @Bean
    public Step userApiImportStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  UserApiItemReader userApiItemReader,
                                  UserItemProcessor userItemProcessor,
                                  UserItemWriter userItemWriter,
                                  BatchJobListener batchJobListener) {
        return new StepBuilder("userApiImportStep", jobRepository)
                .<UserImportDto, User>chunk(10, transactionManager)
                .reader(userApiItemReader)
                .processor(userItemProcessor)
                .writer(userItemWriter)
                .listener(batchJobListener)
                .build();
    }

}