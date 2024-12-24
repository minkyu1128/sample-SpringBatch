package com.minkyu.samplebatch.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.batch.core.JobExecution;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Builder
public class JobExecutionResponse {
    private Long executionId;
    private String jobName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String exitCode;
    private String exitDescription;

    public static JobExecutionResponse from(JobExecution jobExecution) {
        return JobExecutionResponse.builder()
                .executionId(jobExecution.getId())
                .jobName(jobExecution.getJobInstance().getJobName())
                .status(jobExecution.getStatus().name())
                .startTime(Optional.ofNullable(jobExecution.getStartTime())
                        .map(date -> date.toInstant().atZone(
                                java.time.ZoneId.systemDefault()).toLocalDateTime())
                        .orElse(null))
                .endTime(Optional.ofNullable(jobExecution.getEndTime())
                        .map(date -> date.toInstant().atZone(
                                java.time.ZoneId.systemDefault()).toLocalDateTime())
                        .orElse(null))
                .exitCode(jobExecution.getExitStatus().getExitCode())
                .exitDescription(jobExecution.getExitStatus().getExitDescription())
                .build();
    }
}