package com.timeyang.jkes.spring.jpa;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chaokunyang
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(JkesSpringConfig.class)
public @interface EnableJkes {

}
