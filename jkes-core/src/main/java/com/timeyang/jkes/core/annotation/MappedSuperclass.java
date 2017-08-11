package com.timeyang.jkes.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Designates a class whose mapping information is applied
 * to the entities that inherit from it.
 *
 * <p>Currently if a field in subclass is annotated, the super field metadata will be ignored</p>
 * @author chaokunyang
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RUNTIME)
public @interface MappedSuperclass {
}
