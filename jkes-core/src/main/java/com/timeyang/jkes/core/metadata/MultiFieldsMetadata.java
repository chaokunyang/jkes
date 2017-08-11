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
}
