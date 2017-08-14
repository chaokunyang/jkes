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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldMetadata)) return false;

        FieldMetadata that = (FieldMetadata) o;

        if (!method.equals(that.method)) return false;
        if (!field.equals(that.field)) return false;
        return fieldName.equals(that.fieldName);

    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + field.hashCode();
        result = 31 * result + fieldName.hashCode();
        return result;
    }
}
