package com.timeyang.jkes.services.delete.domain;

import lombok.Getter;
import lombok.ToString;

/**
 * Persistence Event
 *
 * @author chaokunyang
 */
public abstract class Event {

    private EventType eventType;

    public Event(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Because all event is async, it's suggested that the client delete the index, recreate index and <strong>restart corresponding es_sink_connector</strong>(es_sink_connector task will fail when index not exist and there is data not consumed in corresponding kafka topic). Kafka will act as buffer, and this is of great performance.
     *
     * <p>Of course you can change DELETE_ALL event to DELETE event, to avoid delete new saved data. But convert DELETE_ALL event to DELETE event may be difficult and poor performance
     </p>
     */
    public enum EventType {
        SAVE,
        // MULTI_SAVE,
        DELETE,
        // MULTI_DELETE,
        DELETE_ALL
    }

    /**
     * DeleteEvent
     * @author chaokunyang
     */
    @Getter
    @ToString
    public static class DeleteEvent extends Event {

        private Object id;

        private String index;

        private String type;

        private Class<?> domainClass;

        public DeleteEvent(EventType eventType, Object id, String index, String type) {
            super(eventType);
            this.id = id;
            this.index = index;
            this.type = type;
        }

        public DeleteEvent(EventType eventType, Object id, String index, String type, Class<?> domainClass) {
            super(eventType);
            this.id = id;
            this.index = index;
            this.type = type;
            this.domainClass = domainClass;
        }

    }

    /**
     * SaveEvent
     * @author chaokunyang
     */
    @Getter
    @ToString
    public static class SaveEvent extends Event {

        private Object value;

        public SaveEvent(EventType eventType, Object value) {
            super(eventType);
            this.value = value;
        }
    }
}
