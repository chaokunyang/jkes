package com.timeyang.jkes.integration_test;

import com.timeyang.jkes.core.exception.JkesException;
import com.timeyang.jkes.core.kafka.connect.KafkaConnectClient;
import com.timeyang.jkes.core.kafka.producer.JkesKafkaProducer;
import com.timeyang.jkes.core.kafka.producer.Topics;
import com.timeyang.jkes.core.kafka.util.KafkaUtils;
import com.timeyang.jkes.integration_test.domain.PersonGroup;
import com.timeyang.jkes.integration_test.repository.PersonGroupRepository;
import com.timeyang.jkes.spring.jpa.ConcurrentIndexer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

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
    private ConcurrentIndexer concurrentIndexer;

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

                    String topic = KafkaUtils.getTopic(PersonGroup.class);
                    if(Topics.contains(topic)) {
                        jkesKafkaProducer.send(pageResult);
                    }else {
                        Iterator<PersonGroup> iterator = pageResult.iterator();
                        if(iterator.hasNext()) {
                            Future<RecordMetadata> future = jkesKafkaProducer.send(iterator.next(),
                                    (metadata, exception) -> {
                                        kafkaConnectClient.createEsSinkConnectorIfAbsent(PersonGroup.class);
                                        Topics.add(topic);
                                    });
                            try {
                                future.get(); // make the callback block, to ensure esSinkConnector exists
                            } catch (InterruptedException | ExecutionException e) {
                                throw new JkesException(e);
                            }
                        }

                        iterator.forEachRemaining(jkesKafkaProducer::send);
                    }
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
        concurrentIndexer.addTask(new ConcurrentIndexer.IndexTask<PersonGroup>() {
            @Override
            public Class<PersonGroup> getEntityClass() {
                return PersonGroup.class;
            }

            @Override
            public long count() {
                return personGroupRepository.count();
            }

            @Override
            public Page<PersonGroup> getData(Pageable pageable) {
                return personGroupRepository.findAll(pageable);
            }
        });

        concurrentIndexer.start();
        try {
            concurrentIndexer.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendData() {
        long start = System.currentTimeMillis();

        int nThreads = 10;
        ExecutorService exec = Executors.newFixedThreadPool(nThreads);
        int sizePerThread = 2_000;

        for(int i = 0; i < nThreads; i++) {
            String topic = KafkaUtils.getTopic(PersonGroup.class);
            for(int j = 0; j < sizePerThread; j++) {
                PersonGroup personGroup = generatePersonGroup(j);
                long id = (long)(i * sizePerThread + j);
                personGroup.setId(id);
                if(Topics.contains(topic)) {
                    jkesKafkaProducer.send(personGroup);
                }else {
                    Future<RecordMetadata> future = jkesKafkaProducer.send(personGroup,
                            (metadata, exception) -> {
                                kafkaConnectClient.createEsSinkConnectorIfAbsent(PersonGroup.class);
                                Topics.add(topic);
                            });
                    try {
                        future.get(); // make the callback block, to ensure esSinkConnector exists

                    } catch (InterruptedException | ExecutionException e) {
                        throw new JkesException(e);
                    }
                }
            }

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