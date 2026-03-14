package io.github.js.batch.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class UserPartitionQuartzJob extends QuartzJobBean {

    // QuartzJobBean은 Quartz가 직접 인스턴스를 생성하므로 생성자 주입 불가 → 필드 주입 사용
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job userPartitionJob;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(userPartitionJob, params);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

}
