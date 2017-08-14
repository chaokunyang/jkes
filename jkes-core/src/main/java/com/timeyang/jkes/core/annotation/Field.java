package com.timeyang.jkes.core.annotation;

import java.lang.annotation.*;

/**
 * 只允许标注在getter方法上，因为待索引数据没有对应值，如果方法和字段都允许标注，判断逻辑很复杂。容易引起混淆，导致编码风格不统一，而且需要设计字段和getter方法的优先级。两轮查找也耗费更多性能
 *
 * @author chaokunyang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD }) // in future, code meta annotation base on ElementType.ANNOTATION_TYPE
@Documented
@Inherited
public @interface Field {

    /**
     * @return Returns the field name. Defaults to the JavaBean property name.
     */
    String name() default "";

    /**
     * Specify how field is indexed
     */
    FieldType type() default FieldType.Auto;

    /**
     * @return analyzer name
     */
    String analyzer() default "";

    /**
     * <p>By default, field values are indexed to make them searchable, but they are not stored. This means that the field can be queried, but the original field value cannot be retrieved.</p>
     * <p>Usually this doesn’t matter. The field value is already part of the _source field, which is stored by default. If you only want to retrieve the value of a single field or of a few fields, instead of the whole _source, then this can be achieved with source filtering.</p>
     <p>In certain situations it can make sense to store a field. For instance, if you have a document with a title, a date, and a very large content field, you may want to retrieve just the title and the date without having to extract those fields from a large _source field</p>
     */
    boolean store() default false;

    // /**
    //  * <P>Sorting, aggregations, and access to field values in scripts requires a different data access pattern. Instead of looking up the term and finding documents, we need to be able to look up the document and find the terms that it has in a field.</P>
    //  * <p>Doc values are the on-disk data structure, built at document index time, which makes this data access pattern possible. They store the same values as the _source but in a column-oriented fashion that is way more efficient for sorting and aggregations. <strong>Doc values are supported on almost all field types, with the notable exception of analyzed string fields.</strong></p>
    //  * <p>
    //  *   All fields which support doc values have them enabled by default. If you are sure that you don’t need to sort or aggregate on a field, or access the field value from a script, you can disable doc values in order to save disk space
    //  * </p>
    //  */
    // boolean doc_values() default true;

    /*
    * specific config
    */

    // /**
    //  * jackson序列化配置的是时间戳
    //  * @return
    //  */
    // DateFormat format() default DateFormat.none;

    // /**
    //  * data format. ex: yyyy-MM-dd . The syntax for these is explained in the Joda docs - http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html.
    //  *
    //  * @see DateFormat
    //  */
    // String pattern() default "";
}
