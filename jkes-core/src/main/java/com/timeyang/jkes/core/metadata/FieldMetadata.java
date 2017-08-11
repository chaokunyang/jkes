package com.timeyang.jkes.core.metadata;

import com.timeyang.jkes.core.annotation.Field;

import java.lang.reflect.Method;

/**
 * @author chaokunyang
 */
public class FieldMetadata {

    private final Method method;
    private final Field field;
    private final String fieldName;

    public FieldMetadata(Method method, Field field, String fieldName) {
        this.method = method;
        this.field = field;
        this.fieldName = fieldName;
    }

    public Method getMethod() {
        return method;
    }

    public Field getField() {
        return field;
    }

    public String getFieldName() {
        return fieldName;
    }
}
