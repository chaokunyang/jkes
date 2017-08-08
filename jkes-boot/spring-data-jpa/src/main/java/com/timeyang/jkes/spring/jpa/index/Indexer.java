package com.timeyang.jkes.spring.jpa.index;

import java.util.concurrent.TimeUnit;

/**
 * @author chaokunyang
 */
public interface Indexer {

    Indexer startAll();
    void start(String entityClassName);
    void shutdown() throws InterruptedException;
    void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
    void addTask(IndexTask<?> task);

}
