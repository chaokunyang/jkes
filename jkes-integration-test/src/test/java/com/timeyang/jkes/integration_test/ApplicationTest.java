package com.timeyang.jkes.integration_test;

import com.timeyang.jkes.core.kafka.connect.KafkaConnectClient;
import com.timeyang.jkes.core.kafka.producer.JkesKafkaProducer;
import com.timeyang.jkes.integration_test.domain.PersonGroup;
import com.timeyang.jkes.integration_test.repository.PersonGroupRepository;
import com.timeyang.jkes.integration_test.repository.PersonRepository;
import com.timeyang.jkes.spring.jpa.index.IndexProgress;
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
import java.util.Collection;
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
    private PersonRepository personRepository;

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
        long start = System.nanoTime();

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

        long elapsed = System.nanoTime() - start;
        System.out.println("elapsed time = " + elapsed / 1000_000 + "ms");
        System.out.println((elapsed / 1000) / (times_per_thread * nThreads) + " microseconds per record");
        System.out.println("queryAndSendData ==> tps = " + (times_per_thread * nThreads) / (elapsed / 1000_000_000));
    }

    // @Test
    public void queryAndSendData() {
        long start = System.nanoTime();

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

        long elapsed = System.nanoTime() - start;
        System.out.println("elapsed time = " + elapsed / 1000_000 + "ms");
        System.out.println((elapsed / 1000) / count + " microseconds per record");
        System.out.println("queryAndSendData ==> tps = " + count / (elapsed / 1000_000_000));
    }

    public void queryAndSend() {
        long start = System.nanoTime();

        indexer.startAll();
        try {
            indexer.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long elapsed = System.nanoTime() - start;

        Long indexed = 0L;
        Collection<IndexProgress> progresses = indexer.getProgress().values();
        for(IndexProgress progress : progresses) {
            indexed += progress.getIndexed();
        }

        System.out.println("elapsed time = " + elapsed / 1000_000 + "ms");
        System.out.println((elapsed / 1000) / indexed + " microseconds per record");
        System.out.println("queryAndSend ==> tps = " + indexed / (elapsed / 1000_000_000));
    }

    public void sendData() {
        long start = System.nanoTime();

        int nThreads = 4;
        ExecutorService exec = Executors.newFixedThreadPool(nThreads);
        int sizePerThread = 10_0000;

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

        long elapsed = System.nanoTime() - start;
        System.out.println("elapsed time = " + elapsed / 1000_000 + "ms");
        System.out.println((elapsed / 1000) / (sizePerThread * nThreads) + " microseconds per record");
        System.out.println("sendData ==> tps = " + (sizePerThread * nThreads) / (elapsed / 1000_000_000));
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