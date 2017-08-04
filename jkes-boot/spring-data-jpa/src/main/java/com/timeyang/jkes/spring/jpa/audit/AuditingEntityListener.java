package com.timeyang.jkes.spring.jpa.audit;


import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

/**
 * AuditingEntityListener
 * @author chaokunyang
 */
public class AuditingEntityListener {

    private AuditingHandler auditingHandler = new AuditingHandler();

    /**
     * @param auditedEntity
     */
    @PrePersist
    public void beforeInsert(Object auditedEntity) {
        auditingHandler.markCreated(auditedEntity);
    }

    /**
     * @param auditedEntity
     */
    @PreUpdate
    public void beforeUpdate(Object auditedEntity) {
        auditingHandler.markModified(auditedEntity);
    }
}