// package com.timeyang.search.core.elasticsearch.annotation;
//
// import java.lang.annotation.*;
//
// /**
//  * 只允许标注在getter方法上，因为待索引数据没有对应值，如果方法和字段都允许标注，判断逻辑很复杂。容易引起混淆，导致编码风格不统一，而且需要设计字段和getter方法的优先级。两轮查找也耗费更多性能
//  *
//  * @author chaokunyang
//  */
// @Retention(RetentionPolicy.RUNTIME)
// @Target({ ElementType.METHOD })
// @Documented
// public @interface DateField {
//
//     /**
//      * @return Returns the field name. Defaults to the JavaBean property name.
//      */
//     String name() default "";
//
//     boolean store() default false;
//
//     boolean doc_values() default true;
//
//     /*
//     * specific config
//     */
//
//     DateFormat format() default DateFormat.none;
//
//     /**
//      * data format. ex: yyyy-MM-dd . The syntax for these is explained in the Joda docs - http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html.
//      *
//      * @see DateFormat
//      */
//     String pattern() default "";
// }
