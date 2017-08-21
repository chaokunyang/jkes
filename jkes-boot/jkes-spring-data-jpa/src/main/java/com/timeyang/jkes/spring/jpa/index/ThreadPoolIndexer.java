package com.timeyang.jkes.spring.jpa.index;

import com.timeyang.jkes.core.kafka.producer.JkesKafkaProducer;
import com.timeyang.jkes.core.metadata.Metadata;
import com.timeyang.jkes.core.util.DocumentUtils;
import com.timeyang.jkes.spring.jpa.ContextSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.HashMap;
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
// @Named
public class ThreadPoolIndexer extends AbstractIndexer implements Indexer {

    private static final Log logger = LogFactory.getLog(ThreadPoolIndexer.class);
    private final static int nThreads = 20;

    private final JkesKafkaProducer jkesKafkaProducer;

    private int pageSize = 200;
    private ExecutorService exec;
    private ConcurrentMap<String, Future<?>> inFlightTasksMap;

    @Inject
    public ThreadPoolIndexer(JkesKafkaProducer jkesKafkaProducer, Metadata metadata, ContextSupport contextSupport) {
        super(metadata, contextSupport);
        this.jkesKafkaProducer = jkesKafkaProducer;

        this.exec = Executors.newFixedThreadPool(nThreads);
        this.inFlightTasksMap = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        super.init();
    }

    @Override
    public ThreadPoolIndexer startAll() {
        logger.info("Start querying data from database and indexing");
        tasksMap.values().forEach(task -> {
            Future<?> submit = submit(task);
            this.inFlightTasksMap.put(task.getDomainClass().getCanonicalName(), submit);
        });

        return this;
    }

    @Override
    public void start(String entityClassName) {
        synchronized (this) {
            IndexTask<?> task = this.tasksMap.get(entityClassName);

            Future<?> future = this.inFlightTasksMap.get(entityClassName);
            if(future != null && !future.isDone()) {
                boolean cancel = future.cancel(true);
                if(cancel)
                    logger.info("Canceled task: " + task);
            }

            Future<?> submit = submit(task);
            this.inFlightTasksMap.put(task.getDomainClass().getCanonicalName(), submit);
            logger.debug("added task: " + task);
        }
    }

    @Override
    public boolean stop(String entityClassName) {
        Future<?> future = this.inFlightTasksMap.get(entityClassName);
        return future != null && future.cancel(true);
    }

    @Override
    public Map<String, Boolean> stopAll() {
        Map<String, Boolean> result = new HashMap<>();
        this.inFlightTasksMap.forEach((k, v) -> {
            result.put(k, v.cancel(true));
        });
        return result;
    }

    private Future<?> submit(IndexTask<?> task) {
        Class<?> entityClass = task.getDomainClass();
        Future<?> future = exec.submit(() -> {
            String index = DocumentUtils.getIndexName(entityClass);
            logger.info("Start full indexing for class[" + entityClass + ", the corresponding index is " + index);

            long count = task.count();
            super.progress.put(entityClass.getCanonicalName(), new IndexProgress(entityClass, count, 0L));

            int pageCount = (int) (count / pageSize);
            if (count % pageSize != 0)
                pageCount++;

            int send = 0;
            for (int p = 0; p <= pageCount; p++) {
                if(Thread.interrupted()) return;

                Pageable pageable = new PageRequest(p, pageSize);
                Page<?> page = task.getData(pageable);

                send += sendData(page);
                super.progress.put(entityClass.getCanonicalName(), new IndexProgress(entityClass, count, (long) send));
            }

            logger.info("The index[" + index + "] for class [" + entityClass + "] has finished full indexing, the total document is " + count);
        });

        return future;
    }

    @PreDestroy
    @Override
    public void shutdown() throws InterruptedException {
        awaitTermination(30, TimeUnit.SECONDS);
    }

    @Override
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

    @Override
    public void addTask(IndexTask<?> task) {
        super.addTask(task);
        logger.debug("added task: " + task);
    }

    @Override
    public Map<String, IndexProgress> getProgress() {
        return null;
    }

}
