package io.github.js.batch.job.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.Duration;

@Slf4j
public class UserImportJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job '{}' starting with parameters: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long totalWriteCount = jobExecution.getStepExecutions().stream()
                .mapToLong(step -> step.getWriteCount())
                .sum();
        long elapsedMs = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis();

        log.info("Job '{}' {} in {}ms, total processed: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                elapsedMs,
                totalWriteCount);
    }

}