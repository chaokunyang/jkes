package com.timeyang.jkes.spring.jpa.index;

import com.timeyang.jkes.core.elasticsearch.indices.IndicesAdminClient;
import com.timeyang.jkes.core.kafka.producer.JkesKafkaProducer;
import com.timeyang.jkes.core.metadata.Metadata;
import com.timeyang.jkes.spring.jpa.ContextSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * Currently Jkes use a ThreadPool to schedule, I will use ForkJoin to schedule, ForkJoin can increase performance.
 *
 * @author chaokunyang
 */
@Named
class ForkJoinIndexer extends AbstractIndexer implements Indexer {

    private static final Log logger = LogFactory.getLog(IndicesAdminClient.class);

    private final JkesKafkaProducer jkesKafkaProducer;
    private static final int PAGE_SIZE = 200;

    private final ConcurrentMap<String, Future<?>> inFlightTasksMap;

    private final ForkJoinPool pool;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    public ForkJoinIndexer(JkesKafkaProducer jkesKafkaProducer, Metadata metadata, ContextSupport contextSupport) {
        super(metadata, contextSupport);

        this.jkesKafkaProducer = jkesKafkaProducer;

        this.inFlightTasksMap = new ConcurrentHashMap<>();

        this.pool = ForkJoinPool.commonPool();
    }

    @PostConstruct
    public void init() {
        super.init();
    }

    @Override
    public ForkJoinIndexer startAll() {
        logger.info("Start querying data from database and indexing");
        super.tasksMap.values().forEach(task -> {
            Class<?> domainClass = task.getDomainClass();

            Future<?> submit = submit(task);
            this.inFlightTasksMap.put(domainClass.getCanonicalName(), submit);

            super.progress.put(domainClass.getCanonicalName(), new IndexProgress(domainClass, task.count(), (long) 0));
        });

        return this;
    }

    @Override
    public void start(String entityClassName) {
        synchronized (this) {
            IndexTask<?> task = this.tasksMap.get(entityClassName);

            Future<?> future = this.inFlightTasksMap.get(entityClassName);
            if(future != null && !future.isDone()) {
                if(future.cancel(true))
                    logger.info("Canceled task: " + task);
            }

            Future<?> submit = submit(task);
            logger.debug("submitted task: " + task);

            Class<?> domainClass = task.getDomainClass();
            super.progress.put(domainClass.getCanonicalName(), new IndexProgress(domainClass, task.count(), (long) 0));

            this.inFlightTasksMap.put(task.getDomainClass().getCanonicalName(), submit);
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
        this.inFlightTasksMap.forEach((k, v) -> result.put(k, v.cancel(true)));
        return result;
    }

    private Future<?> submit(IndexTask<?> task) {
        long count = task.count();

        return pool.submit(new ForkIndexAction(task, 0, (int) count, count));
    }

    @Override
    public Map<String, IndexProgress> getProgress() {
        return Collections.unmodifiableMap(super.progress);
    }

    @PreDestroy
    @Override
    public void shutdown() throws InterruptedException {
        awaitTermination(30, TimeUnit.SECONDS);
    }

    @Override
    public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        this.pool.shutdown();
        this.pool.awaitTermination(timeout, unit);
    }

    private class ForkIndexAction extends RecursiveAction {
        private final IndexTask<?> indexTask;
        private int start;
        private int quantity;
        private final long indexTotal;
        private final int threshold;

        ForkIndexAction(IndexTask<?> indexTask,
                        int start, int quantity, long indexTotal) {
            this.indexTask = indexTask;
            this.start = start;
            this.quantity = quantity;
            this.indexTotal = indexTotal;

            this.threshold = getThreshold(indexTotal);
        }

        private int getThreshold(long total) {
            if(total < 10_000) return 500;
            if(total < 100_000) return 1_000;
            if(total < 1_000_000) return 10_000;
            if(total < 10_000_000) return 100_000;
            if(total < 100_000_000) return 1_000_000;

            return 10_000_000;
        }

        @Override
        protected void compute() {
            if(quantity < threshold) {
                indexDirectly();
                return;
            }

            int split = quantity / 2;

            invokeAll(new ForkIndexAction(indexTask, start, split, indexTotal),
                    new ForkIndexAction(indexTask, start + split, quantity - split, indexTotal));
        }

        void indexDirectly() {
            Class<?> domainClass = indexTask.getDomainClass();

            int offset = start;
            int rest = start + quantity - offset;
            int perSize = Math.min(PAGE_SIZE, rest);

            while (perSize > 0) {
                if(isCancelled()) return; // doesn't work, why?

                int send = sendData(offset, perSize, domainClass);
                synchronized (progress) {
                    IndexProgress indexProgress = progress.get(domainClass.getCanonicalName());
                    Long oldIndexed = indexProgress.getIndexed();
                    long indexed = send + oldIndexed;
                    progress.put(domainClass.getCanonicalName(),
                            new IndexProgress(domainClass, indexProgress.getTotal(), indexed));
                }

                offset += perSize;
                rest = start + quantity - offset;
                perSize = Math.min(PAGE_SIZE, rest);

            }
        }

        private <T> int sendData(int start, int size, Class<T> domainClass) {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();

            // CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
            // Root<T> countRoot = countCriteria.from(domainClass);
            // long indexTotal = this.entityManager.createQuery(
            //         countCriteria.select(builder.count(countRoot))
            // ).getSingleResult();

            CriteriaQuery<T> query = builder.createQuery(domainClass);
            Root<T> pageRoot = query.from(domainClass);
            List<T> list = entityManager.createQuery(
                    query.select(pageRoot)
            ).setFirstResult(start)
                    .setMaxResults(size)
                    .getResultList();

            if (list.size() != 0) {
                jkesKafkaProducer.send(list);
            }
            return list.size();
        }
    }

}
