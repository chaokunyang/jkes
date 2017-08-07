package com.timeyang.jkes.core.kafka.util;

import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.util.Asserts;
import com.timeyang.jkes.core.support.Config;
import com.timeyang.jkes.core.util.DocumentUtils;
import com.timeyang.jkes.core.annotation.DocumentId;
import com.timeyang.jkes.core.elasticsearch.exception.IlllegalSearchStateException;
import com.timeyang.jkes.core.util.StringUtils;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author chaokunyang
 */
public class KafkaUtils {

    public static String getTopicWithoutPrefix(Object entity) {
        Class<?> clazz = entity.getClass();
        Asserts.check(clazz.isAnnotationPresent(Document.class), "The class " + clazz.getCanonicalName() + " of entity " + entity + " must be annotated with " + Document.class.getCanonicalName());

        return getTopicWithoutPrefix(clazz);
    }

    public static String getTopicWithoutPrefix(Class<?> clazz) {
        Asserts.check(clazz.isAnnotationPresent(Document.class), "The class " + clazz.getCanonicalName() + " must be annotated with " + Document.class.getCanonicalName());

        return DocumentUtils.getTypeName(clazz);
    }

    /**
     * Get kafka topic to publish message
     * @param entity the entity to be indexed
     * @return kafka topic to publish message
     */
    public static String getTopic(Object entity) {
        return getTopic(entity.getClass());
    }

    /**
     * Get kafka topic to publish message
     * @param clazz the class of entity to be indexed
     * @return kafka topic to publish message
     */
    public static String getTopic(Class<?> clazz) {
        String topic = getTopicWithoutPrefix(clazz);

        String prefix = Config.getJkesProperties().getClientId();
        if(StringUtils.hasText(prefix)) {
            return prefix + "_" + topic;
        }
        return topic;
    }

    /**
     * Get kafka message key
     * @param entity the entity to be indexed
     * @return kafka message key
     */
    public static String getKey(Object entity) {
        Class<?> domainClass = entity.getClass();
        Asserts.check(domainClass.isAnnotationPresent(Document.class), "The class " + domainClass.getCanonicalName() + " must be annotated with " + Document.class.getCanonicalName());

        Method[] methods = domainClass.getMethods(); // 包括从父类继承的方法
        for(Method method : methods) {
            if(method.isAnnotationPresent(DocumentId.class) ||
                    method.isAnnotationPresent(Id.class)) {
                try {
                    Object key = method.invoke(entity);
                    return String.valueOf(key);
                } catch (IllegalAccessException e) {
                    throw new IlllegalSearchStateException(
                            DocumentId.class + " or " + Id.class + "can only be annotated on public class", e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        Class<?> clazz = entity.getClass();
        do {
            Field[] fields = clazz.getDeclaredFields();
            for(Field field : fields) {
                if (field.isAnnotationPresent(Id.class)) {
                    try {
                        field.setAccessible(true);
                        Object key = field.get(entity);
                        return String.valueOf(key);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e); // impossible, because we set accessibility to true
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }while (clazz != null);

        throw new IlllegalSearchStateException(domainClass + " doesn't have a document id. You either annotated a method with " + DocumentId.class + " , or annotated a field or getter method with " + Id.class);
    }

    public static String getDeleteTopic() {
        return Config.getJkesProperties().getClientId() + "_" + "delete";
    }
}
