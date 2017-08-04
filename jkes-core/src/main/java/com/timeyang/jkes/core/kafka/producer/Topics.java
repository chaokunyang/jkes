package com.timeyang.jkes.core.kafka.producer;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author chaokunyang
 */
public class Topics {

    private static CopyOnWriteArraySet<String> topics = new CopyOnWriteArraySet<>();

    public static boolean add(String topic) {
        return topics.add(topic);
    }

    public static boolean contains(String topic) {
        return topics.contains(topic);
    }
}
