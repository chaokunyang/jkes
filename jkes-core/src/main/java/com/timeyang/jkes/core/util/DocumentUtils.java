package com.timeyang.jkes.core.util;

import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.annotation.DocumentVersion;
import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.MultiFields;
import com.timeyang.jkes.core.elasticsearch.exception.IllegalAnnotatedFieldException;
import com.timeyang.jkes.core.support.Config;

import java.lang.reflect.Method;

/**
 * Document Utils
 *
 * @author chaokunyang
 */
public class DocumentUtils {

    public static String getIndexName(Class<?> clazz) {
        Document document = clazz.getAnnotation(Document.class);
        String indexName = document.indexName();

        if(!StringUtils.hasText(indexName))
            indexName = StringUtils.addUnderscores(clazz.getSimpleName());

        String prefix = Config.getJkesProperties().getClientId();
        if(StringUtils.hasText(prefix)) {
            return prefix + "_" + indexName;
        }

        return indexName;
    }

    public static String getTypeName(Class<?> clazz) {
        Document document = clazz.getAnnotation(Document.class);
        String type = document.type();
        if(!StringUtils.hasText(type))
            type = StringUtils.addUnderscores(clazz.getSimpleName());

        return type;
    }

    public static String getAlias(Class<?> clazz) {
        Document document = clazz.getAnnotation(Document.class);
        String alias = document.alias();
        if(!StringUtils.hasText(alias))
            alias = StringUtils.addUnderscores(clazz.getSimpleName()) + "_alias";

        String prefix = Config.getJkesProperties().getClientId();
        if(StringUtils.hasText(prefix)) {
            return prefix + "_" + alias;
        }

        return alias;
    }

    public static String getFieldName(Method method) {
        Field field = method.getAnnotation(Field.class);
        if (field != null && StringUtils.hasLength(field.name())) {
            return StringUtils.addUnderscores(StringUtils.trimAllWhitespace(field.name()));
        }

        MultiFields multiFields = method.getAnnotation(MultiFields.class);
        if(multiFields != null) {
            Field mainField = multiFields.mainField();
            if(StringUtils.hasText(mainField.name()))
                return StringUtils.addUnderscores(StringUtils.trimAllWhitespace(mainField.name()));
        }

        String methodName = method.getName();
        if(methodName.startsWith("is")) {
            return StringUtils.addUnderscores(methodName.substring(2));
        }else if(methodName.startsWith("get")) {
            return StringUtils.addUnderscores(method.getName().substring(3));
        }else {
            throw new IllegalAnnotatedFieldException(Field.class.getCanonicalName() +
                    "can only be annotated on getter method or isXXX method, " + "can't be annotated on method on ");
        }

    }

    public static String getVersionField(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        for(Method method : methods) {
            if(method.isAnnotationPresent(Field.class) && method.isAnnotationPresent(DocumentVersion.class)) {
                return getFieldName(method);
            }
        }

        return null;
    }

}
