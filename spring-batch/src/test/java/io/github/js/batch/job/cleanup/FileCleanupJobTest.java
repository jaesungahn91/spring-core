package io.github.js.batch.job.cleanup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
class FileCleanupJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    @Qualifier("fileCleanupJob")
    private Job fileCleanupJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(fileCleanupJob);
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    void fileCleanupJob_completedSuccessfully(@TempDir Path tempDir) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("targetDirectory", tempDir.toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void fileCleanupJob_deletesOldFiles(@TempDir Path tempDir) throws Exception {
        Path oldFile = tempDir.resolve("old-file.txt");
        Files.writeString(oldFile, "content");
        oldFile.toFile().setLastModified(
                Instant.now().minus(8, ChronoUnit.DAYS).toEpochMilli()
        );

        Path recentFile = tempDir.resolve("recent-file.txt");
        Files.writeString(recentFile, "content");

        JobParameters params = new JobParametersBuilder()
                .addString("targetDirectory", tempDir.toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        assertThat(Files.exists(oldFile)).isFalse();
        assertThat(Files.exists(recentFile)).isTrue();
    }

}