package com.minkyu.samplebatch.api.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Getter
@Setter
public class JobLaunchRequest {
    @NotBlank(message = "Job 이름은 필수입니다")
    private String jobName;
    private Map<String, String> parameters;

    public JobParameters toJobParameters() {
        JobParametersBuilder builder = new JobParametersBuilder();
        if (parameters != null) {
            parameters.forEach(builder::addString);
        }
        return builder.toJobParameters();
    }
}