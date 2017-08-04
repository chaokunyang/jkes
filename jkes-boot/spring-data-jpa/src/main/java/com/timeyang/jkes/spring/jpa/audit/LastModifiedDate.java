package com.timeyang.jkes.spring.jpa.audit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Declares a field as the one representing the date the entity containing the field was recently modified.
 *
 * @author chaokunyang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { FIELD, METHOD })
public @interface LastModifiedDate {
}
