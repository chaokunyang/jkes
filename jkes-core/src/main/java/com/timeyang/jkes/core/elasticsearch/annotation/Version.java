package com.timeyang.jkes.core.elasticsearch.annotation;

import java.lang.annotation.*;

/**
 * Specifies the version field or property of an entity class that
 * serves as its optimistic lock value.  The version is used to ensure
 * integrity when performing the merge operation and for optimistic
 * concurrency control.
 *
 * <p> Only a single <code>Version</code> property or field
 * should be used per class; applications that use more than one
 * <code>Version</code> property or field will not be portable.
 *
 * @author chaokunyang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Documented
public @interface Version {
}

