package com.timeyang.jkes.core.util;

import com.timeyang.jkes.core.elasticsearch.annotation.Field;
import com.timeyang.jkes.core.elasticsearch.annotation.MultiFields;
import com.timeyang.jkes.core.elasticsearch.exception.IllegalAnnotatedFieldException;

import java.lang.reflect.Method;

/**
 * Field Utils
 * @author chaokunyang
 */
public class FieldUtils {

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

}
