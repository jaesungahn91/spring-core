package io.github.js.batch.job.cleanup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@RequiredArgsConstructor
public class FileCleanupTasklet implements Tasklet {

    private final String targetDirectory;
    private final int retentionDays;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        File directory = new File(targetDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            log.warn("Target directory not found: {}", targetDirectory);
            return RepeatStatus.FINISHED;
        }

        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        File[] files = directory.listFiles();
        int deletedCount = 0;

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.lastModified() < cutoff.toEpochMilli()) {
                    if (file.delete()) {
                        deletedCount++;
                        contribution.incrementWriteCount(1);
                    } else {
                        log.warn("Failed to delete file: {}", file.getAbsolutePath());
                    }
                }
            }
        }

        log.info("Deleted {} files older than {} days from {}", deletedCount, retentionDays, targetDirectory);
        return RepeatStatus.FINISHED;
    }

}