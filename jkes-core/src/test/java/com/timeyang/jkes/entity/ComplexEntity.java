package com.timeyang.jkes.entity;

import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.FieldType;
import lombok.Builder;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author chaokunyang
 */
@Builder
@Setter
public class ComplexEntity {

    // 仅用于测试，不具备实际意义
    // 颜色:车
    private Map<String, Car> cars;
    // 分类:书
    private Map<String, List<Book>> books;

    @Field(type = FieldType.Object)
    public Map<String, Car> getCars() {
        return cars;
    }

    @Field(type = FieldType.Nested)
    public Map<String, List<Book>> getBooks() {
        return books;
    }
}
