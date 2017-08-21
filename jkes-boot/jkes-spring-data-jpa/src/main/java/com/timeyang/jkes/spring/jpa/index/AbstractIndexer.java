package com.timeyang.jkes.spring.jpa.index;

import com.timeyang.jkes.core.exception.JkesException;
import com.timeyang.jkes.core.metadata.Metadata;
import com.timeyang.jkes.core.util.StringUtils;
import com.timeyang.jkes.spring.jpa.ContextSupport;
import com.timeyang.jkes.spring.jpa.exception.NoAvailableRepositoryException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author chaokunyang
 */
public abstract class AbstractIndexer implements Indexer {

    private final Metadata metadata;
    private final ContextSupport contextSupport;

    protected final ConcurrentMap<String, IndexTask<?>> tasksMap;
    protected final ConcurrentMap<String, IndexProgress> progress;

    public AbstractIndexer(Metadata metadata, ContextSupport contextSupport) {
        this.metadata = metadata;
        this.contextSupport = contextSupport;

        this.tasksMap = new ConcurrentHashMap<>();
        this.progress = new ConcurrentHashMap<>();
    }

    protected void init() {
        Set<Class<?>> documentClasses = metadata.getAnnotatedDocuments();
        documentClasses.forEach(clazz -> {
            PagingAndSortingRepository repository = getRepositoryBean(clazz);
            addTask(new IndexTask() {
                @Override
                public Class<?> getDomainClass() {
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
    public void addTask(IndexTask<?> task) {
        tasksMap.put(task.getDomainClass().getCanonicalName(), task);
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
