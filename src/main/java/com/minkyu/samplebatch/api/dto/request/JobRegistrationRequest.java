package com.minkyu.samplebatch.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Map;

@Getter
@Setter
public class JobRegistrationRequest {

    @NotBlank(message = "Job 이름은 필수입니다")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Job 이름은 영문자, 숫자, 하이픈, 언더스코어만 허용됩니다")
    private String jobName;

    @NotBlank(message = "Job 설명은 필수입니다")
    private String description;

    private Map<String, String> defaultParameters;

    private String cronExpression;  // 스케줄링이 필요한 경우
}