package io.github.js.batch.job.user;

import io.github.js.batch.job.BatchJobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * Parallel Steps 예제.
 *
 * 핵심 개념:
 * - 서로 독립적인 Step들을 FlowBuilder.split()으로 병렬 실행
 * - CSV import(userImportStep)와 API import(userApiImportStep)를 동시에 처리
 * - 모든 Flow가 완료된 후 Job 종료
 *
 * Multi-threaded Step과의 차이:
 * - Multi-threaded Step: 하나의 Step 내에서 청크 단위 병렬 처리
 * - Parallel Steps: 서로 다른 Step 자체를 병렬 실행 (각 Step이 독립적인 StepExecution 보유)
 */
@Configuration
public class UserParallelImportJobConfig {

    @Bean
    public Job userParallelImportJob(JobRepository jobRepository,
                                     Flow userParallelImportFlow,
                                     BatchJobListener batchJobListener) {
        return new JobBuilder("userParallelImportJob", jobRepository)
                .listener(batchJobListener)
                .start(userParallelImportFlow)
                .end()
                .build();
    }

    @Bean
    public Flow userParallelImportFlow(@Qualifier("userImportStep") Step userImportStep,
                                       @Qualifier("userApiImportStep") Step userApiImportStep) {
        Flow csvImportFlow = new FlowBuilder<Flow>("csvImportFlow")
                .start(userImportStep)
                .build();

        Flow apiImportFlow = new FlowBuilder<Flow>("apiImportFlow")
                .start(userApiImportStep)
                .build();

        return new FlowBuilder<Flow>("userParallelImportFlow")
                .split(new SimpleAsyncTaskExecutor("parallel-import-"))
                .add(csvImportFlow, apiImportFlow)
                .build();
    }

}
