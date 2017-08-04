package com.timeyang.jkes.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h1>Index the same field in different ways for different purposes</h1>
 * <p>Multi-fields do not change the original _source field.</p>
 *
 * ex:
 * <pre>
curl -XPUT 'localhost:9200/my_index?pretty' -H 'Content-Type: application/json' -d'
 {
 "mappings": {
 "my_type": {
 "properties": {
 "my_field": {
 "type": "text",
 "fields": {
 "Keyword": {
 "type": "Keyword"
 }
 }
 }
 }
 }
 }
 }'
 * </pre>
 *
 * <p>The fields setting is allowed to have different settings for fields of the same name in the same index. New multi-fields can be added to existing fields using the PUT mapping API.</p>
 * @author chaokunyang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MultiFields {

    Field mainField();

    InnerField[] otherFields() default {};

}