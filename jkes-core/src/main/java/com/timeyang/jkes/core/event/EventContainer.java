package com.timeyang.jkes.core.event;

import java.util.LinkedList;

/**
 * @author chaokunyang
 */
public class EventContainer {

    private static final ThreadLocal<LinkedList<Event>> events = new ThreadLocal<LinkedList<Event>>() {
        @Override
        protected LinkedList<Event> initialValue() {
            return new LinkedList<>();
        }
    };

    public static void addEvent(Event event) {
        events.get().add(event);
    }

    public static LinkedList<Event> getEvents() {
        return events.get();
    }

    /**
     * clear event container
     */
    public static void clear() {
        events.remove();
    }

}
