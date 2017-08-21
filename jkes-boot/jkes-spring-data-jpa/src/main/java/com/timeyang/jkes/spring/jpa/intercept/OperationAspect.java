package com.timeyang.jkes.spring.jpa.intercept;

import com.timeyang.jkes.spring.jpa.EventSupport;
import com.timeyang.jkes.spring.jpa.exception.JoinPointProceedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Operation aspect
 *
 * @author chaokunyang
 */
@Aspect
public class OperationAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationAspect.class);

    private EventSupport eventSupport;

    @Inject
    public OperationAspect(EventSupport eventSupport) {
        this.eventSupport = eventSupport;
    }

    @AfterReturning(
            pointcut = "execution(* save(*)) and com.timeyang.jkes.spring.jpa.intercept.OperationPointcut.inRepository()",
            returning = "retVal"
    )
    public void afterSave(Object retVal) {
        eventSupport.addSaveEventIfIndexed(retVal);
    }

    @Around("com.timeyang.jkes.spring.jpa.intercept.OperationPointcut.delete()")
    public Object delete(ProceedingJoinPoint proceedingJoinPoint) {
       eventSupport.addDeleteEventIfIndexed(proceedingJoinPoint);
        try {
            return proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throw new JoinPointProceedException(throwable);
        }
    }

}
