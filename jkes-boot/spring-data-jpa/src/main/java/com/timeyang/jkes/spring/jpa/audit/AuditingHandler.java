package com.timeyang.jkes.spring.jpa.audit;

/**
 * @author chaokunyang
 */

import com.timeyang.jkes.core.util.ReflectionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Date;

/**
 * Auditing Handler
 *
 * @author chaokunyang
 */
public class AuditingHandler {

    private static final Logger LOGGER = LogManager.getLogger(AuditingHandler.class);

    private boolean modifyOnCreation = true;

    public void setModifyOnCreation(boolean modifyOnCreation) {
        this.modifyOnCreation = modifyOnCreation;
    }

    /**
     * mark object created
     * @param auditedEntity
     */
    public void markCreated(Object auditedEntity) {
        touch(auditedEntity, true);
    }

    /**
     * mark object modified
     * @param auditedEntity
     */
    public void markModified(Object auditedEntity) {
        touch(auditedEntity, false);
    }

    private void touch(Object auditedEntity, boolean isNew) {
        touchDate(auditedEntity, isNew);
    }


    /**
     * Set createdDate and lastModifiedDate
     * @param auditedEntity
     * @param isNew
     * @return
     */
    private Date touchDate(Object auditedEntity, boolean isNew) {

        Field createdDate = null;
        Field lastModifiedDate = null;

        Class<?> clazz = auditedEntity.getClass();
        do {
            Field[] fields = clazz.getDeclaredFields();
            for(Field field : fields) {
                if(field.isAnnotationPresent(CreatedDate.class)) {
                    field.setAccessible(true);
                    createdDate = field;
                    if(lastModifiedDate != null) break;
                }

                if(field.isAnnotationPresent(LastModifiedDate.class)) {
                    field.setAccessible(true);
                    lastModifiedDate = field;
                    if(createdDate != null) break;
                }
            }

            if(createdDate == null || lastModifiedDate == null) {
                clazz = clazz.getSuperclass();
            }else {
                break;
            }
        }while (clazz != null);

        if(createdDate == null || lastModifiedDate == null) {
            clazz = auditedEntity.getClass();

            do {
                Method[] methods = auditedEntity.getClass().getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(CreatedDate.class)) {
                        try {
                            createdDate = auditedEntity.getClass().getDeclaredField(ReflectionUtils.getFieldNameForGetter(method));

                            if(lastModifiedDate != null) break;
                        } catch (NoSuchFieldException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (method.isAnnotationPresent(LastModifiedDate.class)) {
                        try {
                            lastModifiedDate = auditedEntity.getClass().getDeclaredField(ReflectionUtils.getFieldNameForGetter(method));

                            if(createdDate != null) break;
                        } catch (NoSuchFieldException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                if(createdDate == null || lastModifiedDate == null) {
                    clazz = clazz.getSuperclass();
                }else {
                    break;
                }

            }while (clazz != null);
        }


        Date now = new Date();

        if(isNew) {
            try {
                if(createdDate != null) {
                    if(createdDate.getType() == Date.class) {
                        createdDate.set(auditedEntity, now);
                    }else if(createdDate.getType() == Instant.class) {
                        createdDate.set(auditedEntity, Instant.ofEpochMilli(now.getTime()));
                    }else {
                        throw new RuntimeException(CreatedDate.class + " can only be annotated on "
                                + Date.class + " or " + Instant.class);
                    }

                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e); // impossible, because we setAccessible(true)
            }

            if(!modifyOnCreation)
                return now; // if create isn't treated as modified, just return
        }

        try {

            if(lastModifiedDate != null) {
                if(lastModifiedDate.getType() == Date.class) {
                    lastModifiedDate.set(auditedEntity, now);
                }else if(lastModifiedDate.getType() == Instant.class) {
                    lastModifiedDate.set(auditedEntity, Instant.ofEpochMilli(now.getTime()));
                }else {
                    throw new RuntimeException(LastModifiedDate.class + "can only be annotated on "
                            + Date.class + " or " + Instant.class);
                }

            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e); // impossible, because we setAccessible(true)
        }

        return now;
    }

}
