package io.github.js.batch.job.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class UserImportJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job '{}' starting with parameters: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Job '{}' completed successfully in {}ms",
                    jobExecution.getJobInstance().getJobName(),
                    Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis());
            logStepSummaries(jobExecution);
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("Job '{}' failed with exceptions: {}",
                    jobExecution.getJobInstance().getJobName(),
                    jobExecution.getAllFailureExceptions());
        }
    }

    private void logStepSummaries(JobExecution jobExecution) {
        jobExecution.getStepExecutions().forEach(step ->
                log.info("Step: {} | Read: {} | Written: {} | Skipped: {}",
                        step.getStepName(),
                        step.getReadCount(),
                        step.getWriteCount(),
                        step.getSkipCount())
        );
    }

}