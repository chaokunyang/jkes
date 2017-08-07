package com.timeyang.jkes.spring.jpa;

import com.timeyang.jkes.core.kafka.producer.JkesKafkaProducer;
import com.timeyang.jkes.core.util.DocumentUtils;
import com.timeyang.jkes.spring.jpa.index.IndexProgress;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
    private ConcurrentMap<String, IndexTask<?>> tasksMap;
    private int pageSize = 200;

    private int nThreads = 20;
    private ExecutorService exec;
    private ConcurrentMap<String, IndexProgress> progress;
    private ConcurrentMap<String, Future<?>> inFlightTasksMap;

    @Inject
    public ConcurrentIndexer(JkesKafkaProducer jkesKafkaProducer) {
        this.tasksMap = new ConcurrentHashMap<>();
        this.jkesKafkaProducer = jkesKafkaProducer;
        this.exec = Executors.newFixedThreadPool(nThreads);
        this.progress = new ConcurrentHashMap<>();
        this.inFlightTasksMap = new ConcurrentHashMap<>();
    }

    public ConcurrentIndexer startAll() {
        LOGGER.info("Start querying data from database and indexing");
        tasksMap.values().forEach(task -> this.inFlightTasksMap.put(task.getEntityClass().getCanonicalName(), submit(task)));

        return this;
    }

    public void start(String entityClassName) {
        synchronized (this) {
            IndexTask<?> task = this.tasksMap.get(entityClassName);

            Future<?> future = this.inFlightTasksMap.get(entityClassName);
            if(!future.isDone()) {
                boolean cancel = future.cancel(true);
                if(cancel)
                    LOGGER.info("Canceled task: " + task);
            }

            this.inFlightTasksMap.put(task.getEntityClass().getCanonicalName(), submit(task));
            LOGGER.debug("added task: " + task);
        }
    }

    private Future<?> submit(IndexTask<?> task) {
        Class<?> entityClass = task.getEntityClass();
        Future<?> future = exec.submit(() -> {
            String index = DocumentUtils.getIndexName(entityClass);
            LOGGER.info("Start full indexing for class[" + entityClass + ", the corresponding index is " + index);

            long count = task.count();
            this.progress.putIfAbsent(entityClass.getCanonicalName(), new IndexProgress(entityClass, count, 0L));

            int pageCount = (int) (count / pageSize);
            if (count % pageSize != 0)
                pageCount++;

            int send = 0;
            for (int p = 0; p <= pageCount; p++) {
                Pageable pageable = new PageRequest(p, pageSize);
                Page<?> page = task.getData(pageable);

                send += sendData(page);
                this.progress.put(entityClass.getCanonicalName(), new IndexProgress(entityClass, count, (long) send));
            }

            LOGGER.info("The index[" + index + "] for class [" + entityClass + "] has finished full indexing, the total document is " + count);
        });

        return future;
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        awaitTermination(30, TimeUnit.SECONDS);
    }

    public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        this.exec.shutdown();
        this.exec.awaitTermination(timeout, unit);
    }

    private int sendData(Page<?> page) {
        List<?> content = page.getContent();
        if (content.size() != 0) {
            jkesKafkaProducer.send(content);
        }
        return content.size();
    }

    public void addTask(IndexTask<?> task) {
        tasksMap.put(task.getEntityClass().getCanonicalName(), task);
        LOGGER.debug("added task: " + task);
    }

    /**
     * Note the map is unmodifiable
     * <p>key: entity class canonicalName. <br/>
     * value: index progress</p>
     * @return progress map
     */
    public Map<String, IndexProgress> getProgress() {
        return Collections.unmodifiableMap(progress);
    }

    public interface IndexTask<T> {

        Class<T> getEntityClass();

        long count();

        Page<T> getData(Pageable pageable);
    }



}
