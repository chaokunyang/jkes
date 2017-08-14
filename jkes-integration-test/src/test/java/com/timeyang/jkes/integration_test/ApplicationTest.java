package com.timeyang.jkes.integration_test;

import com.timeyang.jkes.core.kafka.connect.KafkaConnectClient;
import com.timeyang.jkes.core.kafka.producer.JkesKafkaProducer;
import com.timeyang.jkes.integration_test.domain.PersonGroup;
import com.timeyang.jkes.integration_test.repository.PersonGroupRepository;
import com.timeyang.jkes.spring.jpa.index.Indexer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author chaokunyang
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ApplicationTest {
    @Autowired
    private PersonGroupRepository personGroupRepository;

    @Autowired
    private JkesKafkaProducer jkesKafkaProducer;

    @Autowired
    private KafkaConnectClient kafkaConnectClient;

    @Autowired
    private Indexer indexer;

    @Test
    public void test() {
        // addData();
        // queryAndSendData();
        // queryAndSend();
        sendData();
    }

    // @Test
    public void addData() {
        long start = System.currentTimeMillis();

        int nThreads = 10;
        int times_per_thread = 2_000;
        ExecutorService exec = Executors.newFixedThreadPool(nThreads);
        for(int i = 0; i < nThreads; i++) {
            exec.execute(() -> {
                for(int j = 0; j < times_per_thread; j++) {
                    personGroupRepository.save(generatePersonGroup(j));
                }
            });
        }
        exec.shutdown();
        try {
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("thread interrupted");
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("elapsed time = " + elapsed + "ms");
        System.out.println((elapsed * 1000.0) / (times_per_thread * nThreads) + " microseconds per execution");
        System.out.println("addData ==> qps = " + times_per_thread * nThreads / (elapsed / 1000));
    }

    // @Test
    public void queryAndSendData() {
        long start = System.currentTimeMillis();

        int nThreads = 10;
        ExecutorService exec = Executors.newFixedThreadPool(nThreads);

        long count = personGroupRepository.count();
        long sizeAlloc = count / nThreads;
        int pageSize = 100;
        int pageAlloc = (int) (sizeAlloc / pageSize);

        for(int i = 0; i < nThreads; i++) {
            int thread_number = i;
            exec.execute(() -> {
                int p = thread_number * pageAlloc + 1;
                while(p <= (thread_number + 1) * pageAlloc ) {

                    Pageable pageable = new PageRequest(p, pageSize);
                    Page<PersonGroup> pageResult = personGroupRepository.findAll(pageable);

                    jkesKafkaProducer.send(pageResult);
                    p++;
                }
            });
        }

        exec.shutdown();
        try {
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("thread interrupted");
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("elapsed time = " + elapsed + "ms");
        System.out.println((elapsed * 1000.0) / (count) + " microseconds per record");
        System.out.println("queryAndSendData ==> qps = " + count / (elapsed / 1000));
    }

    public void queryAndSend() {
        long start = System.currentTimeMillis();

        indexer.startAll();
        try {
            indexer.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("elapsed time = " + elapsed + "ms");
    }

    public void sendData() {
        long start = System.currentTimeMillis();

        int nThreads = 4;
        ExecutorService exec = Executors.newFixedThreadPool(nThreads);
        int sizePerThread = 4_0000;

        for(int i = 0; i < nThreads; i++) {
            int thread_number = i;
            exec.execute(() -> {
                for(int j = 0; j < sizePerThread; j++) {
                    PersonGroup personGroup = generatePersonGroup(j);
                    long id = (long)(thread_number * sizePerThread + j);
                    personGroup.setId(id);
                    jkesKafkaProducer.send(personGroup);
                }
            });

        }

        exec.shutdown();
        try {
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("thread interrupted");
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("elapsed time = " + elapsed + "ms");
        System.out.println((elapsed * 1000.0) / (sizePerThread * nThreads) + " microseconds per record");
        System.out.println("sendData ==> qps = " + (sizePerThread * nThreads) / (elapsed / 1000));
    }

    public void deleteData() {
        Iterable<PersonGroup> all = personGroupRepository.findAll(); // easily OutOfMemoryError
        List<PersonGroup> personGroups = new ArrayList<>();
        all.forEach(personGroups::add);

        personGroupRepository.delete(personGroupRepository.findOne(personGroups.get(0).getId()));
        personGroupRepository.delete(personGroups.get(1));
        personGroupRepository.delete(personGroups.get(2).getId());

        personGroups.remove(0);
        personGroups.remove(1);
        personGroups.remove(2);
        personGroupRepository.delete(personGroups);
    }

    private PersonGroup generatePersonGroup(int i) {
        PersonGroup personGroup = new PersonGroup();
        personGroup.setName("name" + i);
        personGroup.setDescription("description" + i);
        personGroup.setInterests("Hadoop, Spark, Flink, Machine Learning" + i);

        return personGroup;
    }
}