package io.github.js.batch.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FileCleanupQuartzJob extends QuartzJobBean {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("fileCleanupJob")
    private Job fileCleanupJob;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String targetDirectory = context.getMergedJobDataMap().getString("targetDirectory");
        long retentionDays = context.getMergedJobDataMap().getLong("retentionDays");
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("targetDirectory", targetDirectory)
                    .addLong("retentionDays", retentionDays)
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(fileCleanupJob, params);
        } catch (Exception e) {
            log.error("Failed to run fileCleanupJob", e);
            throw new JobExecutionException(e);
        }
    }

}