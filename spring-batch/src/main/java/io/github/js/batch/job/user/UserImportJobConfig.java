package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.User;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class UserImportJobConfig {

    @Bean
    public Job userImportJob(JobRepository jobRepository,
                             Step userImportStep,
                             UserImportJobListener jobListener) {
        return new JobBuilder("userImportJob", jobRepository)
                .listener(jobListener)
                .start(userImportStep)
                .build();
    }

    @Bean
    public Step userImportStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               FlatFileItemReader<UserCsvDto> userCsvReader,
                               UserItemProcessor userItemProcessor,
                               JpaItemWriter<User> userItemWriter) {
        return new StepBuilder("userImportStep", jobRepository)
                .<UserCsvDto, User>chunk(10, transactionManager)
                .reader(userCsvReader)
                .processor(userItemProcessor)
                .writer(userItemWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(5)
                .listener(new UserImportStepListener())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<UserCsvDto> userCsvReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        BeanWrapperFieldSetMapper<UserCsvDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(UserCsvDto.class);

        return new FlatFileItemReaderBuilder<UserCsvDto>()
                .name("userCsvReader")
                .resource(new ClassPathResource(inputFile))
                .linesToSkip(1)
                .delimited()
                .names("email", "name", "age")
                .fieldSetMapper(fieldSetMapper)
                .build();
    }

    @Bean
    public UserItemProcessor userItemProcessor() {
        return new UserItemProcessor();
    }

    @Bean
    public JpaItemWriter<User> userItemWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<User>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }

}