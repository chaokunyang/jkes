package com.timeyang.jkes.integration_test;

import com.timeyang.jkes.integration_test.domain.Person;
import com.timeyang.jkes.integration_test.domain.PersonGroup;
import com.timeyang.jkes.integration_test.repository.PersonGroupRepository;
import com.timeyang.jkes.core.kafka.connect.KafkaConnectClient;
import com.timeyang.jkes.core.kafka.producer.JkesKafkaProducer;
import com.timeyang.jkes.spring.jpa.ConcurrentIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author chaokunyang
 */
@SpringBootApplication
@EnableJpaAuditing
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private PersonGroupRepository personGroupRepository;

    @Autowired
    private JkesKafkaProducer jkesKafkaProducer;

    @Autowired
    private KafkaConnectClient kafkaConnectClient;

    @Autowired
    private ConcurrentIndexer concurrentIndexer;
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            addData();
        };
    }

    private void addData() {
        int nThreads = 10;
        int times_per_thread = 1_00;
        ExecutorService exec = Executors.newFixedThreadPool(nThreads);
        for(int i = 0; i < nThreads; i++) {
            int l = i;
            exec.execute(() -> {
                for(int j = 0; j < times_per_thread; j++) {
                    int num = l * times_per_thread + j;
                    PersonGroup personGroup = generatePersonGroup(num);

                    personGroupRepository.save(personGroup);
                }
            });
        }
        exec.shutdown();
        try {
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("thread interrupted");
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