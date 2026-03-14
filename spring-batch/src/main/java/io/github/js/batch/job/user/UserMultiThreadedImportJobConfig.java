package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.User;
import io.github.js.batch.job.BatchJobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Multi-threaded Step 예제.
 *
 * 핵심 개념:
 * - chunk() 단위를 여러 스레드가 병렬로 처리
 * - FlatFileItemReader는 thread-safe하지 않으므로 SynchronizedItemStreamReader로 래핑
 * - 처리 순서 보장이 필요 없는 경우에 적합
 *
 * UserImportJobConfig(단일 스레드)와의 차이:
 * - 동일한 CSV를 여러 스레드가 나눠서 읽고 처리
 * - 청크 단위로 트랜잭션이 분리되어 각 스레드가 독립적으로 commit
 */
@Configuration
public class UserMultiThreadedImportJobConfig {

    @Bean
    public Job userMultiThreadedImportJob(JobRepository jobRepository,
                                          @Qualifier("userMultiThreadedImportStep") Step userMultiThreadedImportStep,
                                          BatchJobListener batchJobListener) {
        return new JobBuilder("userMultiThreadedImportJob", jobRepository)
                .listener(batchJobListener)
                .start(userMultiThreadedImportStep)
                .build();
    }

    @Bean
    public Step userMultiThreadedImportStep(JobRepository jobRepository,
                                            PlatformTransactionManager transactionManager,
                                            SynchronizedItemStreamReader<UserImportDto> synchronizedUserCsvReader,
                                            UserItemProcessor userItemProcessor,
                                            UserItemWriter userItemWriter,
                                            BatchJobListener batchJobListener,
                                            @Qualifier("userImportTaskExecutor") TaskExecutor userImportTaskExecutor) {
        return new StepBuilder("userMultiThreadedImportStep", jobRepository)
                .<UserImportDto, User>chunk(10, transactionManager)
                .reader(synchronizedUserCsvReader)
                .processor(userItemProcessor)
                .writer(userItemWriter)
                .taskExecutor(userImportTaskExecutor)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(5)
                .listener(batchJobListener)
                .build();
    }

    /**
     * FlatFileItemReader는 thread-safe하지 않으므로 SynchronizedItemStreamReader로 래핑.
     * 내부적으로 read() 호출에 synchronized를 적용하여 동시 접근을 직렬화.
     */
    @Bean
    @StepScope
    public SynchronizedItemStreamReader<UserImportDto> synchronizedUserCsvReader(
            @Value("#{jobParameters['inputFile']}") String inputFile,
            ResourceLoader resourceLoader) {
        BeanWrapperFieldSetMapper<UserImportDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(UserImportDto.class);

        FlatFileItemReader<UserImportDto> delegate = new FlatFileItemReaderBuilder<UserImportDto>()
                .name("synchronizedUserCsvReader")
                .resource(resourceLoader.getResource(inputFile))
                .linesToSkip(1)
                .delimited()
                .names("email", "name", "age")
                .fieldSetMapper(fieldSetMapper)
                .build();

        return new SynchronizedItemStreamReaderBuilder<UserImportDto>()
                .delegate(delegate)
                .build();
    }

    @Bean("userImportTaskExecutor")
    public TaskExecutor userImportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("user-import-");
        executor.initialize();
        return executor;
    }

}
