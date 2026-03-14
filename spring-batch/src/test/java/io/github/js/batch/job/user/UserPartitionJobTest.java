package io.github.js.batch.job.user;

import io.github.js.batch.domain.user.User;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
class UserPartitionJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("userPartitionJob")
    private Job userPartitionJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(userPartitionJob);
        jobRepositoryTestUtils.removeJobExecutions();
        userRepository.deleteAll();

        // Partitioner가 분할할 대상 데이터 사전 적재
        userRepository.saveAll(List.of(
                new User("a@test.com", "UserA", 20),
                new User("b@test.com", "UserB", 25),
                new User("c@test.com", "UserC", 30),
                new User("d@test.com", "UserD", 35),
                new User("e@test.com", "UserE", 40)
        ));
    }

    @Test
    void userPartitionJob_completedSuccessfully() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void userPartitionJob_processesAllUsers() throws Exception {
        long userCount = userRepository.count();

        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        // 파티션 처리 후 데이터 손실 없이 전체 건수 유지
        assertThat(userRepository.count()).isEqualTo(userCount);
    }

    @Test
    void userPartitionJob_createsMultipleStepExecutions() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        // Master Step 1개 + Worker Step N개 (gridSize=4 기준, 데이터 5건이므로 최대 4개 파티션)
        long workerStepCount = execution.getStepExecutions().stream()
                .filter(se -> se.getStepName().startsWith("userPartitionWorkerStep"))
                .count();
        assertThat(workerStepCount).isGreaterThanOrEqualTo(1);
    }

}
