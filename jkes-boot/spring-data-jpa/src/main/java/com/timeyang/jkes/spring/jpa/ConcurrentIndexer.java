package com.timeyang.jkes.spring.jpa;

import com.timeyang.jkes.core.kafka.producer.JkesKafkaProducer;
import com.timeyang.jkes.core.util.DocumentUtils;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Currently Jkes use a ThreadPool to schedule, I will use ForkJoin to schedule, ForkJoin can increase performance.
 *
 * @author chaokunyang
 */
@Named
public class ConcurrentIndexer {

    private static final Logger LOGGER = Logger.getLogger(ConcurrentIndexer.class);

    private final JkesKafkaProducer jkesKafkaProducer;
    private LinkedList<IndexTask<?>> tasks;
    private int pageSize = 200;

    private int nThreads = 20;
    private ExecutorService exec;

    @Inject
    public ConcurrentIndexer(JkesKafkaProducer jkesKafkaProducer) {
        this.tasks = new LinkedList<>();
        this.jkesKafkaProducer = jkesKafkaProducer;
        this.exec = Executors.newFixedThreadPool(nThreads);
    }

    public ConcurrentIndexer start() {
        LOGGER.info("Start querying data from database and indexing");
        tasks.forEach(task ->
                exec.execute(() -> {
            Class<?> entityClass = task.getEntityClass();
            String index = DocumentUtils.getIndexName(entityClass);
            LOGGER.info("Start full indexing for class[" + entityClass + ", the corresponding index is " + index);

            long count = task.count();
            int pageCount = (int) (count / pageSize);
            if(count % pageSize != 0)
                pageCount++;
            for(int p = 0; p <= pageCount; p++) {
                Pageable pageable = new PageRequest(p, pageSize);
                Page<?> page = task.getData(pageable);

                sendData(page);
            }

            LOGGER.info("The index[" + index + "] for class [" + entityClass + "] has finished full indexing, the total document is " + count);
        }));


        this.tasks = new LinkedList<>();

        return this;
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        awaitTermination(30, TimeUnit.SECONDS);
    }

    public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        this.exec.shutdown();
        this.exec.awaitTermination(timeout, unit);
    }

    private void sendData(Page<?> page) {
        List<?> content = page.getContent();
        if(content.size() != 0) {
            jkesKafkaProducer.send(content);
        }
    }

    public void addTask(IndexTask<?> task) {
        tasks.add(task);
    }

    public interface IndexTask<T> {

        Class<T> getEntityClass();

        long count();

        Page<T> getData(Pageable pageable);
    }

}
