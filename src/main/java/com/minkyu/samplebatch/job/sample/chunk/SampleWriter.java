package com.minkyu.samplebatch.job.sample.chunk;

import com.minkyu.samplebatch.domain.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

@Component
@RequiredArgsConstructor
public class SampleWriter extends JpaItemWriter<Person> {

    private final EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void init() {
        this.setEntityManagerFactory(entityManagerFactory);
    }
}