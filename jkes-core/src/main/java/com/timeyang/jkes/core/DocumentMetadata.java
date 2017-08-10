package com.timeyang.jkes.core;

import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.Immutable;
import com.timeyang.jkes.core.annotation.MultiFields;

import java.lang.reflect.Method;

/**
 * DocumentMetadata
 *
 * @author chaokunyang
 */
@Immutable
public class DocumentMetadata {

    private Class<?> clazz;
    private Method method;
    private Field field;
    private MultiFields multiFields;

    public DocumentMetadata(Class<?> clazz, Method method, Field field, MultiFields multiFields) {
        this.clazz = clazz;
        this.method = method;
        this.field = field;
        this.multiFields = multiFields;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Method getMethod() {
        return method;
    }

    public Field getField() {
        return field;
    }

    public MultiFields getMultiFields() {
        return multiFields;
    }
}
