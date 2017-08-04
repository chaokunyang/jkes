package com.timeyang.jkes.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chaokunyang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InnerField {

    /**
     * specify a key in child json
     *
     * @return a key in child json
     */
    String suffix();

    FieldType type();

    /**
     * @return analyzer
     */
    String analyzer() default "";

    boolean store() default false;

}
