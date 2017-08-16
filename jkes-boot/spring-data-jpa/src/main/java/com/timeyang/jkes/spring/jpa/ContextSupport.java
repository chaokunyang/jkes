package com.timeyang.jkes.spring.jpa;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.inject.Named;

/**
 * @author chaokunyang
 */
@Named
public class ContextSupport implements ApplicationContextAware {

    private final Object lock = new Object();

    private volatile ApplicationContext applicationContext;

    @Override
    public  void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        synchronized (lock) {
            this.applicationContext = applicationContext;
            lock.notifyAll();
        }
    }

    public Object getBean(String beanName) throws InterruptedException {
        synchronized (lock) {
            while(this.applicationContext == null)
                lock.wait();
        }
        try {
            return this.applicationContext.getBean(beanName);
        }catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    public Object getBean(Class<?> requiredType) throws InterruptedException {
        synchronized (lock) {
            while(this.applicationContext == null)
                lock.wait();
        }
        try {
            return this.applicationContext.getBean(requiredType);
        }catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }
}
