package com.minkyu.samplebatch.common.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchMetrics {

    private final MeterRegistry meterRegistry;

    /**
     * Job 실행 관련 메트릭을 기록합니다.
     */
    public void recordJobExecution(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        String status = jobExecution.getStatus().name();

        // Job 실행 횟수 카운터
        meterRegistry.counter("batch.job.executions",
                        "job.name", jobName,
                        "status", status)
                .increment();

        // Job 실행 시간 측정
        if (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null) {
            long duration = jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime();
            Timer.builder("batch.job.duration")
                    .tag("job.name", jobName)
                    .tag("status", status)
                    .register(meterRegistry)
                    .record(duration, TimeUnit.MILLISECONDS);

            log.debug("Job execution metrics recorded - name: {}, status: {}, duration: {}ms",
                    jobName, status, duration);
        }

        // 읽기/쓰기/처리 항목 수 기록
        jobExecution.getStepExecutions().forEach(stepExecution -> {
            String stepName = stepExecution.getStepName();

            // 읽은 항목 수
            meterRegistry.gauge("batch.step.read.count",
                    Tags.of("job.name", jobName, "step.name", stepName),
                    stepExecution.getReadCount());

            // 쓴 항목 수
            meterRegistry.gauge("batch.step.write.count",
                    Tags.of("job.name", jobName, "step.name", stepName),
                    stepExecution.getWriteCount());

            // 건너뛴 항목 수
            meterRegistry.gauge("batch.step.skip.count",
                    Tags.of("job.name", jobName, "step.name", stepName),
                    stepExecution.getSkipCount());
        });
    }

    /**
     * Job 시작 시간을 기록합니다.
     */
    public void recordJobStart(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        meterRegistry.counter("batch.job.starts",
                        "job.name", jobName)
                .increment();

        log.info("Job started - name: {}, execution id: {}",
                jobName, jobExecution.getId());
    }

    /**
     * Job 종료 시간을 기록합니다.
     */
    public void recordJobEnd(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        String status = jobExecution.getStatus().name();

        meterRegistry.counter("batch.job.ends",
                        "job.name", jobName,
                        "status", status)
                .increment();

        log.info("Job ended - name: {}, execution id: {}, status: {}",
                jobName, jobExecution.getId(), status);
    }
}