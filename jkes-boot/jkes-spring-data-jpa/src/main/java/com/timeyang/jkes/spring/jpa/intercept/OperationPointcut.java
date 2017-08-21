package com.timeyang.jkes.spring.jpa.intercept;

import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation aspect
 *
 * @author chaokunyang
 */
public class OperationPointcut {

    private static final Logger log = LoggerFactory.getLogger(OperationPointcut.class);

    // any join point (method execution only in Spring AOP) where the target implements the CrudRepository interface
    @Pointcut("target(org.springframework.data.repository.CrudRepository)")
    void inRepository() {}
    //
    // @Pointcut("execution(* save(*)) && args(entity) && inRepository()")
    // void saveEntity(Object entity) {}

    // @Pointcut(value = "saveEntity(entity) && inRepository()", argNames = "entity")
    // void allSave(Object entity) {}

    // 'and' can't be used in @Pointcut, must use '&&'. 'and' can only be used and should be used in XML
    @Pointcut("execution(* delete*(..)) && inRepository()")
    void delete() {}

    // @Pointcut("execution(* *.*.(..) && @annotation(com.timeyang.search.core.elasticsearch.annotation.Indexable)")
    // void indexable() {}
    //
    // @Pointcut("execution(* *.*.(..) && @annotation(com.timeyang.search.core.elasticsearch.annotation.Deleteable)")
    // void deleteable() {}

}
