package com.minkyu.samplebatch.job.sample.chunk;

import com.minkyu.samplebatch.domain.Person;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class SampleProcessor implements ItemProcessor<Person, Person> {

    @Override
    public Person process(Person person) {
        person.updateEmail(person.getEmail().toUpperCase());
        return person;
    }
}