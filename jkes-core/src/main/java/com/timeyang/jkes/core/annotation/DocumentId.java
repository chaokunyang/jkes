package com.timeyang.jkes.core.annotation;

import java.lang.annotation.*;

/**
 * Specify document id
 * <p>
 *     both document id and kafka message key rely on it
 * </p>
 * @see Field
 * @author chaokunyang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Documented
public @interface DocumentId {
}

