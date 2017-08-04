package com.timeyang.jkes.entity;

import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.FieldType;
import lombok.AllArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chaokunyang
 */
@Setter
@AllArgsConstructor
@Embeddable
public class Book {
    private String id;
    private String name;
    private Author author;
    private Map<Integer, Collection<String>> buckets = new HashMap<>();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Field(type = FieldType.Object)
    public Author getAuthor() {
        return author;
    }

    @Field(type = FieldType.Nested)
    public Map<Integer, Collection<String>> getBuckets() {
        return buckets;
    }
}
