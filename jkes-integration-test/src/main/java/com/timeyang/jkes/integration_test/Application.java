package com.timeyang.jkes.integration_test;

import com.timeyang.jkes.integration_test.domain.Person;
import com.timeyang.jkes.integration_test.domain.PersonGroup;
import com.timeyang.jkes.integration_test.repository.PersonGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author chaokunyang
 */
@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private PersonGroupRepository personGroupRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            // addData();
            // deleteData();
        };
    }

    private void deleteData() {
        Iterable<PersonGroup> all = personGroupRepository.findAll();
        Iterator<PersonGroup> iterator = all.iterator();

        if(iterator.hasNext())
            personGroupRepository.delete(iterator.next());
        if(iterator.hasNext())
            personGroupRepository.delete(iterator.next().getId());

        iterator.forEachRemaining(
                personGroupRepository::delete
        );
    }

    private void addData() {
        for(int i = 0; i < 10000; i++) {
            personGroupRepository.save(generatePersonGroup(i));
        }
    }

    private PersonGroup generatePersonGroup(int i) {
        PersonGroup personGroup = new PersonGroup();
        personGroup.setName("name" + i);
        personGroup.setDescription("description" + i);
        personGroup.setInterests("Hadoop, Spark, Flink, Machine Learning" + i);

        Person member1 = new Person();
        member1.setName("item" + i);
        member1.setDescription("AI Developer" + i);
        member1.setAge(24);
        member1.setGender("male");
        member1.setPersonGroup(personGroup);
        Person member2 = new Person();
        member2.setName("item" + i);
        member1.setDescription("deep learning Developer" + i);
        member1.setAge(34);
        member1.setGender("female");
        member2.setPersonGroup(personGroup);

        personGroup.setPersons(Arrays.asList(member1, member2));
        return personGroup;
    }
}