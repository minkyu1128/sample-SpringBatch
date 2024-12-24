package com.minkyu.samplebatch.api.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class JobResponse {
    private Long jobExecutionId;
    private Long jobInstanceId;
    private String jobName;
    private BatchStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String exitCode;
    private String exitDescription;
    private Map<String, Object> jobParameters;
    private int readCount;
    private int writeCount;
    private int skipCount;

    public static JobResponse from(JobExecution jobExecution) {
        JobInstance jobInstance = jobExecution.getJobInstance();
        JobParameters params = jobExecution.getJobParameters();

        Map<String, Object> parameters = new HashMap<>();
        params.getParameters().forEach((key, value) -> parameters.put(key, value.getValue()));

        // Step 실행 통계 집계
        int totalReadCount = 0;
        int totalWriteCount = 0;
        int totalSkipCount = 0;

        for (var stepExecution : jobExecution.getStepExecutions()) {
            totalReadCount += stepExecution.getReadCount();
            totalWriteCount += stepExecution.getWriteCount();
            totalSkipCount += stepExecution.getSkipCount();
        }

        return JobResponse.builder()
                .jobExecutionId(jobExecution.getId())
                .jobInstanceId(jobInstance.getId())
                .jobName(jobInstance.getJobName())
                .status(jobExecution.getStatus())
                .startTime(convertToLocalDateTime(jobExecution.getStartTime()))
                .endTime(convertToLocalDateTime(jobExecution.getEndTime()))
                .exitCode(jobExecution.getExitStatus().getExitCode())
                .exitDescription(jobExecution.getExitStatus().getExitDescription())
                .jobParameters(parameters)
                .readCount(totalReadCount)
                .writeCount(totalWriteCount)
                .skipCount(totalSkipCount)
                .build();
    }

    private static LocalDateTime convertToLocalDateTime(java.util.Date date) {
        return date != null ? date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime() : null;
    }
}