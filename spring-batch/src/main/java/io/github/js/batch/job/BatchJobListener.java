package io.github.js.batch.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class BatchJobListener implements JobExecutionListener, StepExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job '{}' starting with parameters: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long totalWriteCount = jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getWriteCount)
                .sum();
        String elapsed = jobExecution.getEndTime() != null
                ? Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis() + "ms"
                : "N/A";

        log.info("Job '{}' {} in {}, total written: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                elapsed,
                totalWriteCount);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Step '{}' completed - read: {}, write: {}, filter: {}, skip: {}",
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getFilterCount(),
                stepExecution.getSkipCount());
        return stepExecution.getExitStatus();
    }
}