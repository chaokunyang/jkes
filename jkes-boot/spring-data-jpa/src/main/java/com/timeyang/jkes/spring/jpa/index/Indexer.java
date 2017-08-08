package com.timeyang.jkes.spring.jpa.index;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Indexer
 * @author chaokunyang
 */
public interface Indexer {

    Indexer startAll();
    void start(String entityClassName);
    boolean stop(String entityClassName);
    Map<String, Boolean> stopAll();
    void shutdown() throws InterruptedException;
    void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
    void addTask(IndexTask<?> task);

    /**
     * Get index progress.
     * <p>Note index progress is immutable</p>
     * @return index progress
     */
    Map<String, IndexProgress> getProgress();

}
