package com.minkyu.samplebatch.job.sample;

import com.minkyu.samplebatch.domain.Person;
import com.minkyu.samplebatch.job.sample.chunk.SampleProcessor;
import com.minkyu.samplebatch.job.sample.chunk.SampleReader;
import com.minkyu.samplebatch.job.sample.chunk.SampleWriter;
import com.minkyu.samplebatch.job.sample.listener.SampleJobListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SampleJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SampleReader reader;
    private final SampleProcessor processor;
    private final SampleWriter writer;
    private final SampleJobListener jobListener;

    @Bean
    public Job sampleJob() {
        return new JobBuilder("sampleJob")
                .repository(jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(sampleStep())
                .build();
    }

    @Bean
    public Step sampleStep() {
        return new StepBuilder("sampleStep")
                .repository(jobRepository)
                .transactionManager(transactionManager)
                .<Person, Person>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}