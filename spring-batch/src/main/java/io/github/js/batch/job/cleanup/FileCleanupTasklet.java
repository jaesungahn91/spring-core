package io.github.js.batch.job.cleanup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class FileCleanupTasklet implements Tasklet {

    private final String targetDirectory;
    private final long retentionDays;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Path directory = Paths.get(targetDirectory);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            log.warn("Target directory not found: {}", targetDirectory);
            return RepeatStatus.FINISHED;
        }

        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int deletedCount = 0;
        int failedCount = 0;

        try (Stream<Path> stream = Files.list(directory)) {
            for (Path file : (Iterable<Path>) stream::iterator) {
                if (!Files.isRegularFile(file)) continue;
                if (Files.getLastModifiedTime(file).toInstant().isBefore(cutoff)) {
                    try {
                        Files.delete(file);
                        deletedCount++;
                        contribution.incrementWriteCount(1);
                    } catch (IOException e) {
                        failedCount++;
                        log.warn("Failed to delete file: {}", file, e);
                    }
                }
            }
        }

        log.info("Deleted {} files, failed {} files older than {} days from {}",
                deletedCount, failedCount, retentionDays, targetDirectory);

        if (failedCount > 0) {
            chunkContext.getStepContext().getStepExecution()
                    .setExitStatus(new ExitStatus("COMPLETED_WITH_FAILURES",
                            failedCount + " file(s) could not be deleted"));
        }

        return RepeatStatus.FINISHED;
    }

}