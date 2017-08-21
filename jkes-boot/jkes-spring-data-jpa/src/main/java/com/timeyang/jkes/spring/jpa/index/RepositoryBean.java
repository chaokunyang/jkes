package com.timeyang.jkes.spring.jpa.index;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chaokunyang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RepositoryBean {

    String beanName() default "";

    Class<?> beanType() default void.class;

}
