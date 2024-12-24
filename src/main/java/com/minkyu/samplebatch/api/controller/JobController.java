package com.minkyu.samplebatch.api.controller;

import com.minkyu.samplebatch.api.dto.request.JobLaunchRequest;
import com.minkyu.samplebatch.api.dto.request.JobRegistrationRequest;
import com.minkyu.samplebatch.api.dto.response.JobExecutionResponse;
import com.minkyu.samplebatch.api.exception.ErrorResponse;
import com.minkyu.samplebatch.api.service.JobManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

@Tag(name = "Batch Job API", description = "배치 작업 관리 API")
@Validated
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobManagementService jobManagementService;

    @Operation(summary = "Job 등록", description = "새로운 배치 작업을 등록합니다.")
    @PostMapping
    public ResponseEntity<JobExecutionResponse> registerJob(
            @Valid @RequestBody JobRegistrationRequest request) {
        return ResponseEntity.ok(jobManagementService.registerJob(request));
    }


    @Operation(
            summary = "Job 실행",
            description = "배치 작업을 실행합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Job 실행 성공",
                            content = @Content(schema = @Schema(implementation = JobExecutionResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PostMapping("/{jobName}/execute")
    public ResponseEntity<JobExecutionResponse> executeJob(
            @PathVariable @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_-]+$") String jobName,
            @Valid @RequestBody(required = false) JobLaunchRequest request) throws Exception {
        if (request == null) {
            request = new JobLaunchRequest();
        }
        request.setJobName(jobName);
        return ResponseEntity.ok(jobManagementService.launchJob(jobName, request));
    }

    @Operation(summary = "Job 상태 조회", description = "배치 작업의 실행 상태를 조회합니다.")
    @GetMapping("/{jobName}/status/{executionId}")
    public ResponseEntity<JobExecutionResponse> getJobStatus(
            @PathVariable @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_-]+$") String jobName,
            @PathVariable @Positive Long executionId) {
        return ResponseEntity.ok(jobManagementService.getJobStatus(jobName, executionId));
    }

    @Operation(summary = "Job 실행 이력 조회", description = "배치 작업의 실행 이력을 조회합니다.")
    @GetMapping("/{jobName}/executions")
    public ResponseEntity<Page<JobExecutionResponse>> getJobExecutions(
            @PathVariable @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_-]+$") String jobName,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(jobManagementService.getJobExecutions(jobName, pageable));
    }

    @Operation(summary = "Job 중지", description = "실행 중인 배치 작업을 중지합니다.")
    @PostMapping("/{jobName}/stop/{executionId}")
    public ResponseEntity<Void> stopJob(
            @PathVariable @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_-]+$") String jobName,
            @PathVariable @Positive Long executionId) throws Exception {
        jobManagementService.stopJob(jobName, executionId);
        return ResponseEntity.ok().build();
    }
}