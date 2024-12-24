package com.minkyu.samplebatch.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class JobRequest {

    @NotBlank(message = "Job 이름은 필수입니다")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Job 이름은 영문자, 숫자, 하이픈, 언더스코어만 허용됩니다")
    private String jobName;

    private Map<String, String> parameters = new HashMap<>();

    public JobParameters toJobParameters() {
        JobParametersBuilder builder = new JobParametersBuilder();

        if (parameters != null) {
            parameters.forEach((key, value) -> {
                if (value != null) {
                    // 숫자형 파라미터 처리
                    if (value.matches("^\\d+$")) {
                        builder.addLong(key, Long.parseLong(value));
                    }
                    // 날짜형 파라미터 처리
                    else if (value.matches("^\\d{4}-\\d{2}-\\d{2}.*")) {
                        try {
                            builder.addDate(key, java.sql.Date.valueOf(value.substring(0, 10)));
                        } catch (IllegalArgumentException e) {
                            builder.addString(key, value);
                        }
                    }
                    // 나머지는 문자열로 처리
                    else {
                        builder.addString(key, value);
                    }
                }
            });
        }

        // 실행 시간 파라미터 추가
        builder.addLong("timestamp", System.currentTimeMillis());

        return builder.toJobParameters();
    }
}