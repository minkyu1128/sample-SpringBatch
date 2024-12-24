package com.minkyu.samplebatch.api.service;

import com.minkyu.samplebatch.api.dto.request.JobLaunchRequest;
import com.minkyu.samplebatch.api.dto.request.JobRegistrationRequest;
import com.minkyu.samplebatch.api.dto.response.JobExecutionResponse;
import com.minkyu.samplebatch.common.exception.BatchException;
import com.minkyu.samplebatch.common.monitoring.BatchMetrics;
import com.minkyu.samplebatch.domain.Person;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobManagementService {

    private final JobLauncher jobLauncher;
    private final JobRepository jobRepository;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    private final JobRegistry jobRegistry;
    private final BatchMetrics batchMetrics;


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void init() {
        log.info("Initializing JobManagementService");
    }


    /**
     * 새로운 Job을 등록합니다.
     */
    @Transactional
    public JobExecutionResponse registerJob(JobRegistrationRequest request) {
        try {
            log.info("Registering new job: {}", request.getJobName());

            // 이미 등록된 Job인지 확인
            if (jobRegistry.getJobNames().contains(request.getJobName())) {
                throw new BatchException("JOB_ALREADY_EXISTS",
                        "Job already exists with name: " + request.getJobName());
            }

            // Job 설정 생성 및 등록
            Job job = createJob(request);
            jobRegistry.register(new ReferenceJobFactory(job));

            log.info("Job registered successfully: {}", request.getJobName());

            // 초기 Job 실행 정보 반환
            return JobExecutionResponse.builder()
                    .jobName(request.getJobName())
                    .status("REGISTERED")
                    .build();

        } catch (Exception e) {
            log.error("Failed to register job: {}", request.getJobName(), e);
            throw new BatchException("JOB_REGISTRATION_FAILED",
                    "Failed to register job: " + request.getJobName(), e);
        }
    }
        private Job createJob(JobRegistrationRequest request) {
        try {
            // Step 생성
            Step step = createSampleStep(request);

            // Job 생성
            return jobBuilderFactory.get(request.getJobName())
                    .incrementer(new RunIdIncrementer())
                    .listener(new JobExecutionListener() {
                        @Override
                        public void beforeJob(JobExecution jobExecution) {
                            log.info("Starting job: {}", request.getJobName());
                        }

                        @Override
                        public void afterJob(JobExecution jobExecution) {
                            log.info("Completed job: {} with status: {}",
                                    request.getJobName(),
                                    jobExecution.getStatus());
                        }
                    })
                    .flow(step)
                    .end()
                    .build();
        } catch (Exception e) {
            throw new BatchException("JOB_CREATION_FAILED",
                    "Failed to create job: " + request.getJobName(), e);
        }
    }


    private Step createSampleStep(JobRegistrationRequest request) {
        return stepBuilderFactory.get(request.getJobName() + "Step")
                .<Person, Person>chunk(10)
                .reader(createReader())
                .processor(createProcessor())
                .writer(createWriter())
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        log.info("Starting step: {}", stepExecution.getStepName());
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        log.info("Completed step: {} with status: {}",
                                stepExecution.getStepName(),
                                stepExecution.getStatus());
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    private JpaPagingItemReader<Person> createReader() {
        JpaPagingItemReader<Person> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setPageSize(10);
        reader.setQueryString("SELECT p FROM Person p");
        reader.setName("personReader");
        try {
            reader.afterPropertiesSet();
        } catch (Exception e) {
            throw new BatchException("READER_CREATION_FAILED",
                    "Failed to create item reader", e);
        }
        return reader;
    }

    private ItemProcessor<Person, Person> createProcessor() {
        return person -> {
            person.updateEmail(person.getEmail().toUpperCase());
            return person;
        };
    }

    private JpaItemWriter<Person> createWriter() {
        JpaItemWriter<Person> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        try {
            writer.afterPropertiesSet();
        } catch (Exception e) {
            throw new BatchException("WRITER_CREATION_FAILED",
                    "Failed to create item writer", e);
        }
        return writer;
    }


    /**
     * Job을 실행합니다.
     */
    @Transactional
    public JobExecutionResponse launchJob(String jobName, JobLaunchRequest request) {
        try {
            log.info("Starting job: {} with parameters: {}", jobName, request);

            Job job = jobRegistry.getJob(jobName);
            JobParameters jobParameters = createJobParameters(request);

            validateJobParameters(job, jobParameters);
            validateJobCanRun(jobName, jobParameters);

            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            batchMetrics.recordJobExecution(jobExecution);

            log.info("Job launched successfully: {} (execution id: {})",
                    jobName, jobExecution.getId());

            return JobExecutionResponse.from(jobExecution);

        } catch (Exception e) {
            log.error("Failed to launch job: {}", jobName, e);
            throw new BatchException("JOB_LAUNCH_FAILED",
                    String.format("Failed to launch job: %s", jobName), e);
        }
    }

    /**
     * Job의 현재 상태를 조회합니다.
     */
    @Transactional(readOnly = true)
    public JobExecutionResponse getJobStatus(String jobName, Long executionId) {
        try {
            JobExecution jobExecution = Optional.ofNullable(
                            jobExplorer.getJobExecution(executionId))
                    .orElseThrow(() -> new BatchException("JOB_NOT_FOUND",
                            "Job execution not found: " + executionId));

            validateJobName(jobName, jobExecution);

            return JobExecutionResponse.from(jobExecution);

        } catch (BatchException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get job status: {} (execution id: {})",
                    jobName, executionId, e);
            throw new BatchException("JOB_STATUS_FETCH_FAILED", "Failed to get job status", e);
        }
    }

    /**
     * Job의 실행 이력을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<JobExecutionResponse> getJobExecutions(String jobName, Pageable pageable) {
        try {
            int start = (int) pageable.getOffset();
            int end = start + pageable.getPageSize();

            List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, start, end);
            List<JobExecutionResponse> executions = jobInstances.stream()
                    .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                    .map(JobExecutionResponse::from)
                    .collect(Collectors.toList());

            long totalExecutions = jobExplorer.getJobInstanceCount(jobName);

            return new PageImpl<>(executions, pageable, totalExecutions);

        } catch (Exception e) {
            log.error("Failed to get job executions: {}", jobName, e);
            throw new BatchException("JOB_HISTORY_FETCH_FAILED",
                    "Failed to get job execution history", e);
        }
    }

    /**
     * 실행 중인 Job을 중지합니다.
     */
    @Transactional
    public void stopJob(String jobName, Long executionId) {
        try {
            log.info("Attempting to stop job: {} (execution id: {})", jobName, executionId);

            JobExecution jobExecution = Optional.ofNullable(
                            jobExplorer.getJobExecution(executionId))
                    .orElseThrow(() -> new BatchException("JOB_NOT_FOUND",
                            "Job execution not found: " + executionId));

            validateJobName(jobName, jobExecution);
            validateJobCanBeStopped(jobExecution);

            jobOperator.stop(executionId);

            log.info("Job stopped successfully: {} (execution id: {})", jobName, executionId);

        } catch (BatchException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to stop job: {} (execution id: {})",
                    jobName, executionId, e);
            throw new BatchException("JOB_STOP_FAILED",
                    "Failed to stop job execution", e);
        }
    }

    private JobParameters createJobParameters(JobLaunchRequest request) {
        return new JobParametersBuilder(request.toJobParameters())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }

    private void validateJobParameters(Job job, JobParameters parameters) {
        JobParametersValidator validator = job.getJobParametersValidator();
        if (validator != null) {
            try {
                validator.validate(parameters);
            } catch (JobParametersInvalidException e) {
                throw new BatchException("INVALID_JOB_PARAMETERS",
                        "Invalid job parameters", e);
            }
        }
    }

    private void validateJobCanRun(String jobName, JobParameters parameters) {
        if (jobRepository.isJobInstanceExists(jobName, parameters)) {
            throw new BatchException("JOB_INSTANCE_EXISTS",
                    "Job instance already exists with these parameters");
        }
    }

    private void validateJobName(String jobName, JobExecution jobExecution) {
        if (!jobExecution.getJobInstance().getJobName().equals(jobName)) {
            throw new BatchException("JOB_NAME_MISMATCH",
                    "Job name does not match the execution");
        }
    }

    private void validateJobCanBeStopped(JobExecution jobExecution) {
        BatchStatus status = jobExecution.getStatus();
        if (!status.isRunning()) {
            throw new BatchException("JOB_NOT_RUNNING",
                    "Job is not in a running state: " + status);
        }
    }
}