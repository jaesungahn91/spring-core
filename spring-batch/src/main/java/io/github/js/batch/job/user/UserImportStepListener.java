package io.github.js.batch.job.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
public class UserImportStepListener implements StepExecutionListener {

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Step completed - read: {}, write: {}, filter: {}, skip: {}",
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getFilterCount(),
                stepExecution.getSkipCount());
        return stepExecution.getExitStatus();
    }

}
