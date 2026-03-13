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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class UserImportJobConfig {

    @Bean
    public Job userImportJob(JobRepository jobRepository,
                             @Qualifier("userImportStep") Step userImportStep,
                             BatchJobListener batchJobListener) {
        return new JobBuilder("userImportJob", jobRepository)
                .listener(batchJobListener)
                .start(userImportStep)
                .build();
    }

    @Bean
    public Step userImportStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               FlatFileItemReader<UserImportDto> userCsvReader,
                               UserItemProcessor userItemProcessor,
                               UserItemWriter userItemWriter,
                               BatchJobListener batchJobListener) {
        return new StepBuilder("userImportStep", jobRepository)
                .<UserImportDto, User>chunk(10, transactionManager)
                .reader(userCsvReader)
                .processor(userItemProcessor)
                .writer(userItemWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(5)
                .listener(batchJobListener)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<UserImportDto> userCsvReader(
            @Value("#{jobParameters['inputFile']}") String inputFile,
            ResourceLoader resourceLoader) {
        BeanWrapperFieldSetMapper<UserImportDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(UserImportDto.class);

        return new FlatFileItemReaderBuilder<UserImportDto>()
                .name("userCsvReader")
                .resource(resourceLoader.getResource(inputFile))
                .linesToSkip(1)
                .delimited()
                .names("email", "name", "age")
                .fieldSetMapper(fieldSetMapper)
                .build();
    }

}