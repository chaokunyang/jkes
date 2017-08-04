package com.timeyang.jkes.core.annotation;

import java.lang.annotation.*;

/**
 * Specifies that an entity is to be indexed by ElasticSearch
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
@Documented
public @interface Document {

    /**
     * index name in elasticsearch. default is class lowercase name
     * @return index name in elasticsearch
     */
    String indexName() default "";

    /**
     * type in elasticsearch
     * @return type in index
     */
    String type() default "";

    /**
     * index alias
     * @return alias in elasticsearch
     */
    String alias() default "";

}
