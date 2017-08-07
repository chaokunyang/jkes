package com.timeyang.jkes.spring.jpa;

import com.timeyang.jkes.spring.jpa.exception.UnsupportedEventPublishException;
import com.timeyang.jkes.spring.jpa.util.SimpleAopUtil;
import com.timeyang.jkes.core.util.Asserts;
import com.timeyang.jkes.core.util.DocumentUtils;
import com.timeyang.jkes.core.util.ReflectionUtils;
import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.elasticsearch.indices.IndicesAdminClient;
import com.timeyang.jkes.core.event.Event;
import com.timeyang.jkes.core.event.EventContainer;
import com.timeyang.jkes.core.exception.EventPublishException;
import com.timeyang.jkes.core.exception.IllegalMemberAccessException;
import com.timeyang.jkes.core.exception.ReflectiveInvocationTargetException;
import com.timeyang.jkes.core.exception.JkesException;
import com.timeyang.jkes.core.kafka.connect.KafkaConnectClient;
import com.timeyang.jkes.core.kafka.producer.JkesKafkaProducer;
import com.timeyang.jkes.core.kafka.producer.Topics;
import com.timeyang.jkes.core.kafka.util.EsKafkaConnectUtils;
import com.timeyang.jkes.core.kafka.util.EsKafkaUtils;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.CrudRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Event Support
 * <p>Currently only support intercept org.springframework.data.repository.CrudRepository api event. And repository impl proxy super class must be {@link SimpleJpaRepository}, we use domain class form {@link SimpleJpaRepository} when adding deleteById event</p>
 *
 * @author chaokunyang
 */
@Named
public class EventSupport {

    private final JkesKafkaProducer jkesKafkaProducer;
    private final IndicesAdminClient indicesAdminClient;
    private final KafkaConnectClient kafkaConnectClient;

    @PersistenceContext
    private EntityManager em;

    @Inject
    public EventSupport(JkesKafkaProducer jkesKafkaProducer, IndicesAdminClient indicesAdminClient, KafkaConnectClient kafkaConnectClient) {
        this.jkesKafkaProducer = jkesKafkaProducer;
        this.indicesAdminClient = indicesAdminClient;
        this.kafkaConnectClient = kafkaConnectClient;
    }

    /**
     * Add delete event if corresponding document is indexed. If document is not indexed, then do nothing
     *
     * @param proceedingJoinPoint join point
     */
    @SuppressWarnings("uchecked")
    public void addDeleteEventIfIndexed(ProceedingJoinPoint proceedingJoinPoint) {
        Object proxy = proceedingJoinPoint.getThis();

        Object target;
        try {
            target = SimpleAopUtil.getTargetObject(proxy);
        } catch (Exception e) {
            throw new JkesException(e);
        }

        Asserts.check(target instanceof CrudRepository, "The target object doesn't implement " + CrudRepository.class);

        if(target instanceof SimpleJpaRepository) {
            SimpleJpaRepository<?, ? extends Serializable> simpleJpaRepository = (SimpleJpaRepository<?, ? extends Serializable>) target;
            JpaEntityInformation<?, ? extends Serializable> entityInformation;
            try {
                Field field = simpleJpaRepository.getClass().getDeclaredField("entityInformation");
                field.setAccessible(true);
                entityInformation = (JpaEntityInformation<?, ? extends Serializable>) field.get(simpleJpaRepository);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            Class<?> domainClass = entityInformation.getJavaType();
            Class<? extends Serializable> idType = entityInformation.getIdType();

            if(domainClass.isAnnotationPresent(Document.class)) {
                Object[] args = proceedingJoinPoint.getArgs();
                if(args != null) {
                    if(args.length == 0) {
                        deleteAll(domainClass, new LinkedList<>());
                    }else if(args.length == 1) {
                        Object arg = args[0];
                        if(arg.getClass() == idType) {
                            // deleteById can't cascade publish delete event,
                            // so first findOne(id), then use deleteEntity to cascade publish delete event
                            // Object e = simpleJpaRepository.findOne((Serializable)arg); // CrudMethodMetadata metadata is advised, and call simpleJpaRepository's method will skip normal setter step, and this will cause NullPointException
                            Object e = findOne(domainClass, arg);
                            deleteEntity(e, new LinkedList<>());
                        }else if(arg.getClass() == domainClass) {
                            deleteEntity(arg, new LinkedList<>());
                        }else if(arg instanceof Iterable) {
                            deleteIterable((Iterable)arg, new LinkedList<>());
                        }
                    }else {
                        throw new UnsupportedEventPublishException("The intercept of delete method in repository can support most one parameter. Currently we only support intercept method in " + CrudRepository.class);
                    }
                }
            }
        }

    }

    /**
     * Add save event if corresponding document is indexed. If document is not indexed, then do nothing
     *
     * @param retVal return value
     */
    public void addSaveEventIfIndexed(Object retVal) {
        if(retVal != null && retVal.getClass().isAnnotationPresent(Document.class)) {
            if(retVal instanceof Iterable) {
                save((Iterable<?>)retVal, new LinkedList<>());
            }else {
                save(retVal, new LinkedList<>());
            }
        }
    }

    /**
     * Handle event and clear event after transaction is committed
     */
    public void handleAndClearEvent() {
        LinkedList<Event> events = EventContainer.getEvents();
        for (Event event : events) {
            if(event.getEventType() == Event.EventType.SAVE) {
                Event.SaveEvent saveEvent = (Event.SaveEvent) event;

                Class<?> domainClass = saveEvent.getValue().getClass();
                String topic = EsKafkaUtils.getTopic(domainClass);
                if(Topics.contains(topic)) {
                    jkesKafkaProducer.send(saveEvent.getValue());
                }else {
                    Future<RecordMetadata> future = jkesKafkaProducer.send(saveEvent.getValue(),
                            (metadata, exception) -> {
                                kafkaConnectClient.createEsSinkConnectorIfAbsent(domainClass);
                                Topics.add(topic);
                            });
                    try {
                        future.get(); // make the callback block, to ensure esSinkConnector exists
                    } catch (InterruptedException | ExecutionException e) {
                        throw new JkesException(e);
                    }

                }

            }else if(event.getEventType() == Event.EventType.DELETE) {
                Event.DeleteEvent deleteEvent = (Event.DeleteEvent) event;
                jkesKafkaProducer.send("delete", "", deleteEvent);
            }else if(event.getEventType() == Event.EventType.DELETE_ALL) {
                Event.DeleteAllEvent deleteAllEvent = (Event.DeleteAllEvent) event;
                Class<?> domainClass = deleteAllEvent.getDomainClass();

                // delete index
                String indexName = deleteAllEvent.getIndex();
                indicesAdminClient.deleteIndex(indexName);
                // recreate index
                indicesAdminClient.createIndex(domainClass);

                // restart connector
                String connectorName = EsKafkaConnectUtils.getConnectorName(domainClass);
                kafkaConnectClient.restartConnector(connectorName);
            }
        }

        EventContainer.clear();
    }

    /**
     * Clear event after transaction rollback
     */
    public void clearEvent() {
        EventContainer.clear();
    }

    private void save(Object entity, LinkedList<Object> context) {
        context.addLast(entity);

        EventContainer.addEvent(new Event.SaveEvent(Event.EventType.SAVE, entity));

        Collection<Object> cascadeEntity = getCascadeEntity(entity, context);
        cascadeEntity.forEach((e) -> {
            if(e instanceof Iterable) {
                save((Iterable)e, context);
            }else {
                save(e, context);
            }
        });

        context.removeLast();
    }

    private void save(Iterable<?> entities, LinkedList<Object> context) {
        entities.forEach(entity -> this.save(entity, context));
    }

    // deleteById can't cascade publish delete event, so first findOne(id), then use deleteEntity to cascade publish delete event
    // private void deleteById(Object id, Class<?> domainClass) {
    //     String index = DocumentUtils.getIndexName(domainClass);
    //     String type = DocumentUtils.getTypeName(domainClass);
    //
    //     EventContainer.addEvent(new Event.DeleteEvent(Event.EventType.DELETE, id, index, type));
    // }

    /**
     * get entity by id
     * <p>Because transaction is managed by spring, we shouldn't manage transaction here</p>
     * @param domainClass domainClass
     * @param id entity id
     * @return entity with specified id
     */
    private Object findOne(Class<?> domainClass, Object id) {
        Object o = em.find(domainClass, id);
        return o;
    }

    private void deleteEntity(Object entity, LinkedList<Object> context) {
        context.addLast(entity);

        Object id = ReflectionUtils.getAnnotatedFieldValue(entity, Id.class);
        if(id == null)
        id = ReflectionUtils.getAnnotatedMethodReturnValue(entity, Id.class);
        if(id == null)
        throw new EventPublishException("Can't get entity id from " + entity);

        Class<?> domainClass = entity.getClass();
        String index = DocumentUtils.getIndexName(domainClass);
        String type = DocumentUtils.getTypeName(domainClass);

        EventContainer.addEvent(new Event.DeleteEvent(Event.EventType.DELETE, id, index, type, null));

        Collection<Object> cascadeEntity = getCascadeEntity(entity, context);
        cascadeEntity.forEach((e) -> {
            if(e instanceof Iterable) {
                deleteIterable((Iterable)e, context);
            }else {
                deleteEntity(e, context);
            }
        });

        context.removeLast();
}

    private void deleteIterable(Iterable<?> entities, LinkedList<Object> context) {
        entities.forEach(entity -> this.deleteEntity(entity, context));
}

    private void deleteAll(Class<?> domainClass, LinkedList<Class<?>> context) {
        context.addLast(domainClass);

        String index = DocumentUtils.getIndexName(domainClass);
        String type = DocumentUtils.getTypeName(domainClass);
        EventContainer.addEvent(new Event.DeleteAllEvent(Event.EventType.DELETE_ALL, index, type, domainClass));

        Set<Class<?>> cascadeClasses = getCascadeClass(domainClass, context);
        cascadeClasses.forEach(clazz -> deleteAll(clazz, context));

        context.removeLast();
    }

    private Collection<Object> getCascadeEntity(Object entity, LinkedList<Object> context) {
        // Use map to address JavaBean without overriding equals and hashCode() correctly. If JavaBean override equals and hashCode() correctly, use Set() is HashSet is best choice.
        Map<String, Object> entityMap = new HashMap<>();

        // because jpa annotation is annotated on public method when annotated on method, so there is no need to use getDeclaredMethods and loop
        Method[] methods = entity.getClass().getMethods();
        for(Method method : methods) {
            CascadeType[] cascadeTypes = getCascadeTypes(method);

            if(cascadeTypes != null) {
                for(CascadeType cascadeType : cascadeTypes) {
                    if(cascadeType == CascadeType.ALL
                            || cascadeType == CascadeType.PERSIST || cascadeType == CascadeType.MERGE) {
                        try {
                            Object o = method.invoke(entity);
                            if(!context.contains(o)) entityMap.put(ReflectionUtils.getFieldNameForGetter(method), o);
                            break;
                        } catch (IllegalAccessException e) {
                            throw new IllegalMemberAccessException(e);
                        } catch (InvocationTargetException e) {
                            throw new ReflectiveInvocationTargetException(e);
                        }
                    }
                }
            }
        }

        Class<?> clazz = entity.getClass();
        do {
            Field[] fields = clazz.getDeclaredFields();
            for(Field field : fields) {
                CascadeType[] cascadeTypes = getCascadeTypes(field);

                if(cascadeTypes != null) {
                    for(CascadeType cascadeType : cascadeTypes) {
                        if(cascadeType == CascadeType.ALL
                                || cascadeType == CascadeType.PERSIST || cascadeType == CascadeType.MERGE) {
                            try {
                                field.setAccessible(true);
                                Object o = field.get(entity);
                                if(!context.contains(o)) entityMap.put(field.getName(), o);
                                break;
                            } catch (IllegalAccessException e) {
                                throw new IllegalMemberAccessException(e); // impossible cause of field.setAccessible(true)
                            }
                        }
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }while (clazz != null);

        return entityMap.values();
    }

    private Set<Class<?>> getCascadeClass(Class<?> domainClass, LinkedList<Class<?>> context) {
        Set<Class<?>> domainClassSet = new HashSet<>();

        // because jpa annotation is annotated on public method when annotated on method, so there is no need to use getDeclaredMethods and loop
        Method[] methods = domainClass.getMethods();
        for(Method method : methods) {
            CascadeType[] cascadeTypes = getCascadeTypes(method);

            if(cascadeTypes != null) {
                for(CascadeType cascadeType : cascadeTypes) {
                    if(cascadeType == CascadeType.ALL || cascadeType == CascadeType.REMOVE) {
                        // method return type may be List<xxx.XXX>, but what we need is xxx.XXX
                        Class<?> dClass = ReflectionUtils.getInnermostTypeClass(method);

                        if(!context.contains(dClass)) {
                            domainClassSet.add(dClass);
                        }

                        break;
                    }
                }
            }
        }

        Class<?> clazz = domainClass;
        do {
            Field[] fields = clazz.getDeclaredFields();
            for(Field field : fields) {
                CascadeType[] cascadeTypes = getCascadeTypes(field);

                if(cascadeTypes != null) {
                    for(CascadeType cascadeType : cascadeTypes) {
                        if(cascadeType == CascadeType.ALL || cascadeType == CascadeType.REMOVE) {
                            Class<?> innermostTypeClass = ReflectionUtils.getInnermostTypeClass(field);
                            if(!context.contains(innermostTypeClass)) domainClassSet.add(innermostTypeClass);

                            break;
                        }
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }while (clazz != null);

        return domainClassSet;
    }

    private CascadeType[] getCascadeTypes(AccessibleObject accessibleObject) {
        CascadeType[] cascadeTypes = null;
        if(accessibleObject.isAnnotationPresent(OneToMany.class)) {
            cascadeTypes = accessibleObject.getAnnotation(OneToMany.class).cascade();
        }else if(accessibleObject.isAnnotationPresent(ManyToOne.class)) {
            cascadeTypes = accessibleObject.getAnnotation(ManyToOne.class).cascade();
        }else if(accessibleObject.isAnnotationPresent(ManyToMany.class)) {
            cascadeTypes = accessibleObject.getAnnotation(ManyToMany.class).cascade();
        }
        return cascadeTypes;
    }

}
