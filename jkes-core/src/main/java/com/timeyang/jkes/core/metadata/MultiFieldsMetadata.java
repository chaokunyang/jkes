package com.timeyang.jkes.core.metadata;

import com.timeyang.jkes.core.annotation.MultiFields;

import java.lang.reflect.Method;

/**
 * @author chaokunyang
 */
public class MultiFieldsMetadata {

    private final Method method;
    private final MultiFields multiFields;
    private final String fieldName;

    public MultiFieldsMetadata(Method method, MultiFields multiFields, String fieldName) {
        this.method = method;
        this.multiFields = multiFields;
        this.fieldName = fieldName;
    }

    public Method getMethod() {
        return method;
    }

    public MultiFields getMultiFields() {
        return multiFields;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiFieldsMetadata that = (MultiFieldsMetadata) o;

        if (!method.equals(that.method)) return false;
        if (!multiFields.equals(that.multiFields)) return false;
        return fieldName.equals(that.fieldName);

    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + multiFields.hashCode();
        result = 31 * result + fieldName.hashCode();
        return result;
    }
}
