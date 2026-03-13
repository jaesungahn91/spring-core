package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
class UserApiImportJobTest {

    @TestConfiguration
    static class ReaderConfig {
        @Bean
        @StepScope
        UserApiItemReader userApiItemReader() {
            return new SimulatedUserApiItemReader();
        }
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("userApiImportJob")
    private Job userApiImportJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(userApiImportJob);
        jobRepositoryTestUtils.removeJobExecutions();
        userRepository.deleteAll();
    }

    @Test
    void userApiImportJob_completedSuccessfully() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void userApiImportJob_savesAllItems() throws Exception {
        jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters());

        assertThat(userRepository.count()).isEqualTo(25);
    }

}