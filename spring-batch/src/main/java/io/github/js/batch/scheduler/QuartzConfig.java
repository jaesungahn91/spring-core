package io.github.js.batch.scheduler;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz JobDetail + Trigger 등록.
 *
 * @Scheduled 대비 Quartz의 핵심 차이:
 * - JobDetail/Trigger 가 DB(QRTZ_* 테이블)에 영속화됨
 * - 다중 인스턴스 환경에서 단 하나의 인스턴스만 실행됨 (클러스터링)
 * - 앱 다운 중 놓친 실행(misfire) 처리 정책 정의 가능
 *
 * 파라미터 전달 방식:
 * - @Value 로 주입받은 설정값 → JobDataMap 에 저장
 * - Spring Batch Job 빈 → QuartzJobBean 에서 @Autowired 로 직접 주입
 *   (Job 빈은 직렬화 불가능하므로 JobDataMap 에 넣을 수 없음)
 */
@Configuration
public class QuartzConfig {

    @Value("${batch.user-import.input-file}")
    private String userImportInputFile;

    @Value("${batch.user-import.cron}")
    private String userImportCron;

    @Value("${batch.user-api-import.cron}")
    private String userApiImportCron;

    @Value("${batch.cleanup.target-directory}")
    private String cleanupTargetDirectory;

    @Value("${batch.cleanup.retention-days}")
    private long cleanupRetentionDays;

    @Value("${batch.cleanup.cron}")
    private String cleanupCron;

    // ── userImportJob ──────────────────────────────────────────────────────

    @Bean
    public JobDetail userImportJobDetail() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("inputFile", userImportInputFile);

        return JobBuilder.newJob(UserImportQuartzJob.class)
                .withIdentity("userImportQuartzJob")
                .usingJobData(dataMap)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger userImportJobTrigger(JobDetail userImportJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(userImportJobDetail)
                .withIdentity("userImportJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(userImportCron)
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    // ── userApiImportJob ───────────────────────────────────────────────────

    @Bean
    public JobDetail userApiImportJobDetail() {
        return JobBuilder.newJob(UserApiImportQuartzJob.class)
                .withIdentity("userApiImportQuartzJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger userApiImportJobTrigger(JobDetail userApiImportJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(userApiImportJobDetail)
                .withIdentity("userApiImportJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(userApiImportCron)
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    // ── fileCleanupJob ─────────────────────────────────────────────────────

    @Bean
    public JobDetail fileCleanupJobDetail() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("targetDirectory", cleanupTargetDirectory);
        dataMap.put("retentionDays", cleanupRetentionDays);

        return JobBuilder.newJob(FileCleanupQuartzJob.class)
                .withIdentity("fileCleanupQuartzJob")
                .usingJobData(dataMap)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger fileCleanupJobTrigger(JobDetail fileCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(fileCleanupJobDetail)
                .withIdentity("fileCleanupJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(cleanupCron)
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

}