package com.minkyu.samplebatch.job.sample.chunk;

import com.minkyu.samplebatch.domain.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

@Component
@RequiredArgsConstructor
public class SampleReader extends JpaPagingItemReader<Person> {

    private final EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void init() {
        this.setEntityManagerFactory(entityManagerFactory);
        this.setPageSize(10);
        this.setQueryString("SELECT p FROM Person p");
        this.setName("personReader");
    }
}