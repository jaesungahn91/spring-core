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
class UserMultiThreadedImportJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("userMultiThreadedImportJob")
    private Job userMultiThreadedImportJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(userMultiThreadedImportJob);
        jobRepositoryTestUtils.removeJobExecutions();
        userRepository.deleteAll();
    }

    @Test
    void userMultiThreadedImportJob_completedSuccessfully() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "classpath:data/users.csv")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void userMultiThreadedImportJob_savesOnlyValidUsers() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "classpath:data/users.csv")
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        // users.csv: 6행 중 invalid 2건 → 유효 4건
        assertThat(userRepository.count()).isEqualTo(4);
    }

}
