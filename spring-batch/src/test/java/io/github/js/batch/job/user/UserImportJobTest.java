package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
class UserImportJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("userImportJob")
    private Job userImportJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(userImportJob);
        jobRepositoryTestUtils.removeJobExecutions();
        userRepository.deleteAll();
    }

    @Test
    void userImportJob_completedSuccessfully() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "data/users.csv")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void userImportJob_savesOnlyValidUsers() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "data/users.csv")
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        // users.csv: 6행 중 invalid email 1건, invalid age 1건 → 유효 4건
        assertThat(userRepository.count()).isEqualTo(4);
    }

    @Test
    void userImportJob_stepExecution_countsCorrect() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "data/users.csv")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        StepExecution stepExecution = execution.getStepExecutions().iterator().next();

        assertThat(stepExecution.getReadCount()).isEqualTo(6);
        assertThat(stepExecution.getWriteCount()).isEqualTo(4);
        assertThat(stepExecution.getFilterCount()).isEqualTo(2);
    }

}