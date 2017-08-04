package com.timeyang.jkes.core.elasticsearch;

import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.elasticsearch.indices.InvalidIndexNameException;
import com.timeyang.jkes.core.util.StringUtils;
import com.timeyang.jkes.core.util.DocumentUtils;

import java.util.Set;

/**
 * check annotation config of class annotate with {@link Document}
 *
 * @author chaokunyang
 */
public class AnnotationConfigChecker {

    /**
     * check annotation config of class annotate with {@link Document}
     * @param annotatedClass class annotate with {@link Document}
     */
    public static void check(Class<?> annotatedClass) {
        String index = DocumentUtils.getIndexName(annotatedClass);
        checkIndex(index);

        String typeName = DocumentUtils.getTypeName(annotatedClass);
        checkType(typeName);

        String alias = DocumentUtils.getAlias(annotatedClass);
        checkAlias(alias);
    }

    /**
     * check annotation config of classes annotate with {@link Document}
     * @param annotatedClasses classes annotate with {@link Document}
     */
    public static void check(Set<Class<?>> annotatedClasses) {
        annotatedClasses.forEach(AnnotationConfigChecker::check);
    }

    private static void checkIndex(String index) {
        if(index.length() >= 255) {
            throw new InvalidIndexNameException("index name[" + index + "] length shouldn't exceed 255 characters");
        }
        if(StringUtils.containsUpperCase(index))
            throw new InvalidIndexNameException("index name[" + index + "] should be all lowerCase");
        if(StringUtils.startsWithAny(index, "_", "-", "+"))
            throw new InvalidIndexNameException("index name[" + index + "] shouldn't starts with _,-,+");
    }

    private static void checkType(String type) {
        if(type.length() >= 255) {
            throw new InvalidIndexNameException("type name[" + type + "] length shouldn't exceed 255 characters");
        }
        if(StringUtils.containsUpperCase(type))
            throw new InvalidIndexNameException("type name[" + type + "] should be all lowerCase");
        if(StringUtils.startsWithAny(type, "_", "-", "+"))
            throw new InvalidIndexNameException("type name[" + type + "] shouldn't starts with _,-,+");
    }

    private static void checkAlias(String alias) {
        if(alias.length() >= 255) {
            throw new InvalidIndexNameException("alias name[" + alias + "] length shouldn't exceed 255 characters");
        }
        if(StringUtils.containsUpperCase(alias))
            throw new InvalidIndexNameException("alias name[" + alias + "] should be all lowerCase");
        if(StringUtils.startsWithAny(alias, "_", "-", "+"))
            throw new InvalidIndexNameException("alias name[" + alias + "] shouldn't starts with _,-,+");
    }

}
