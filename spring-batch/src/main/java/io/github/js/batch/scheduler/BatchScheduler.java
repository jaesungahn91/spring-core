package io.github.js.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job userImportJob;

    @Scheduled(cron = "0 0 2 * * ?")
    public void runUserImportJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("inputFile", "data/users.csv")
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(userImportJob, params);
        } catch (Exception e) {
            log.error("Failed to run userImportJob", e);
        }
    }

}