package com.timeyang.jkes.spring.jpa.index;

import com.timeyang.jkes.core.Metadata;
import com.timeyang.jkes.core.elasticsearch.indices.IndicesAdminClient;
import com.timeyang.jkes.core.exception.JkesException;
import com.timeyang.jkes.core.kafka.producer.JkesKafkaProducer;
import com.timeyang.jkes.core.util.DocumentUtils;
import com.timeyang.jkes.core.util.StringUtils;
import com.timeyang.jkes.spring.jpa.ContextSupport;
import com.timeyang.jkes.spring.jpa.exception.NoAvailableRepositoryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class ConcurrentIndexer implements Indexer {

    private static final Log logger = LogFactory.getLog(IndicesAdminClient.class);

    private final JkesKafkaProducer jkesKafkaProducer;
    private final Metadata metadata;
    private final ContextSupport contextSupport;
    private ConcurrentMap<String, IndexTask<?>> tasksMap;
    private int pageSize = 200;

    private int nThreads = 20;
    private ExecutorService exec;
    private ConcurrentMap<String, IndexProgress> progress;
    private ConcurrentMap<String, Future<?>> inFlightTasksMap;

    @Inject
    public ConcurrentIndexer(JkesKafkaProducer jkesKafkaProducer, Metadata metadata, ContextSupport contextSupport) {
        this.jkesKafkaProducer = jkesKafkaProducer;
        this.metadata = metadata;
        this.contextSupport = contextSupport;

        this.tasksMap = new ConcurrentHashMap<>();
        this.exec = Executors.newFixedThreadPool(nThreads);
        this.progress = new ConcurrentHashMap<>();
        this.inFlightTasksMap = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        Set<Class<?>> documentClasses = metadata.getAnnotatedDocuments();
        documentClasses.forEach(clazz -> {
            PagingAndSortingRepository repository = getRepositoryBean(clazz);
            addTask(new IndexTask() {
                @Override
                public Class<?> getEntityClass() {
                    return clazz;
                }

                @Override
                public long count() {
                    return repository.count();
                }

                @Override
                public Page<?> getData(Pageable pageable) {
                    return repository.findAll(pageable);
                }
            });

            this.progress.put(clazz.getCanonicalName(), new IndexProgress(clazz, repository.count(), (long) 0));
        });
    }

    @Override
    public ConcurrentIndexer startAll() {
        logger.info("Start querying data from database and indexing");
        tasksMap.values().forEach(task -> {
            Future<?> submit = submit(task);
            this.inFlightTasksMap.put(task.getEntityClass().getCanonicalName(), submit);
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
            this.inFlightTasksMap.put(task.getEntityClass().getCanonicalName(), submit);
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
        Class<?> entityClass = task.getEntityClass();
        Future<?> future = exec.submit(() -> {
            String index = DocumentUtils.getIndexName(entityClass);
            logger.info("Start full indexing for class[" + entityClass + ", the corresponding index is " + index);

            long count = task.count();
            this.progress.put(entityClass.getCanonicalName(), new IndexProgress(entityClass, count, 0L));

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
        tasksMap.put(task.getEntityClass().getCanonicalName(), task);
        logger.debug("added task: " + task);
    }

    @Override
    public Map<String, IndexProgress> getProgress() {
        return Collections.unmodifiableMap(this.progress);
    }

    private PagingAndSortingRepository getRepositoryBean(Class<?> entityClass) {
        String className = entityClass.getSimpleName();
        String repositoryBeanName = Character.toLowerCase(className.charAt(0)) + className.substring(1) + "Repository";
        String daoBeanName = Character.toLowerCase(className.charAt(0)) + className.substring(1) + "Dao";

        try {
            Object bean = this.contextSupport.getBean(repositoryBeanName);

            if(bean == null)
                bean = this.contextSupport.getBean(daoBeanName);

            if(bean == null) {
                RepositoryBean annotation = entityClass.getAnnotation(RepositoryBean.class);
                if(annotation != null) {
                    if(StringUtils.hasText(annotation.beanName())) {
                        bean = this.contextSupport.getBean(annotation.beanName());
                        if (bean == null)
                            throw new NoAvailableRepositoryException("Couldn't find repository bean[" + annotation.beanName() + "]");

                        return (PagingAndSortingRepository) bean;
                    }
                    if(annotation.beanType() != void.class) {
                        bean = this.contextSupport.getBean(annotation.beanType());
                        if (bean == null)
                            throw new NoAvailableRepositoryException("Couldn't find repository bean[" + annotation.beanType() + "]");

                        return (PagingAndSortingRepository) bean;
                    }
                }
            }

            if (bean == null)
                throw new NoAvailableRepositoryException(String.format("Couldn't find repository bean with name %s or %s", repositoryBeanName, daoBeanName));
            return (PagingAndSortingRepository) bean;
        } catch (InterruptedException e) {
            throw new JkesException("failed get bean: " + repositoryBeanName, e);
        }

    }

}
