package com.timeyang.jkes.core;

import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.Immutable;
import com.timeyang.jkes.core.annotation.MultiFields;
import com.timeyang.jkes.core.support.JkesProperties;
import com.timeyang.jkes.core.util.ClassUtils;
import com.timeyang.jkes.core.util.ReflectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author chaokunyang
 */
@Immutable
@Named // TODO REMOVE
public class Metadata {

    private final Set<Class<?>> annotatedDocuments;

    private final  Map<Class<?>, Set<DocumentMetadata>> metadata;

    private Metadata(Set<Class<?>> annotatedDocuments, Map<Class<?>, Set<DocumentMetadata>> metadata) {
        this.annotatedDocuments = Collections.unmodifiableSet(annotatedDocuments);
        this.metadata = Collections.unmodifiableMap(metadata);
    }

    // TODO REMOVE
    @Inject
    public Metadata(JkesProperties jkesProperties) {
        Metadata m = new MetadataBuilder(jkesProperties).build();
        this.annotatedDocuments = m.getAnnotatedDocuments();
        this.metadata = m.getMetadata();
    }

    /**
     * Get set of class annotated with {@link Document}
     * @return Get set of class annotated with {@link Document}
     */
    public Set<Class<?>> getAnnotatedDocuments() {
        return annotatedDocuments;
    }

    /**
     * Get documents metadata of all class annotated with {@link Document}
     * @return documents metadata
     */
    public Map<Class<?>, Set<DocumentMetadata>> getMetadata() {
        return metadata;
    }

    @Named
    public static class MetadataBuilder implements Provider<Metadata> {

        private JkesProperties jkesProperties;

        @Inject
        public MetadataBuilder(JkesProperties jkesProperties) {
            this.jkesProperties = jkesProperties;
        }

        public Metadata build() {
            Set<Class<?>> annotatedClasses = ClassUtils.getAnnotatedClasses(jkesProperties.getDocumentBasePackage(), Document.class);

            Map<Class<?>, Set<DocumentMetadata>> documentsMetadata = buildDocumentsMetadata(annotatedClasses);

            return new Metadata(annotatedClasses, documentsMetadata);
        }

        private Map<Class<?>, Set<DocumentMetadata>> buildDocumentsMetadata(Set<Class<?>> annotatedClasses) {
            Map<Class<?>, Set<DocumentMetadata>> map = new HashMap<>();

            annotatedClasses.forEach(clazz -> {
                Set<DocumentMetadata> methodSet = new LinkedHashSet<>();
                map.put(clazz, methodSet);

                Method[] methods = clazz.getMethods(); // include superclass methods
                for(Method method : methods) {
                    Field field = method.getAnnotation(Field.class);

                    if(field == null) {
                        String methodName = method.getName();
                        if(methodName.startsWith("get") || methodName.startsWith("is")) {
                            String memberFieldName = ReflectionUtils.getFieldNameForGetter(methodName);
                            field = ReflectionUtils.getFieldAnnotation(clazz, memberFieldName, Field.class);
                        }
                    }
                    if(field != null) {
                        DocumentMetadata metadata = new DocumentMetadata(clazz, method, field, null);
                        methodSet.add(metadata);
                    }else {
                        MultiFields multiFields = method.getAnnotation(MultiFields.class);
                        if(multiFields == null) {
                            String methodName = method.getName();
                            if(methodName.startsWith("get") || methodName.startsWith("is")) {
                                String memberFieldName = ReflectionUtils.getFieldNameForGetter(methodName);
                                multiFields = ReflectionUtils.getFieldAnnotation(clazz, memberFieldName, MultiFields.class);
                            }
                        }
                        if(multiFields != null) {
                            DocumentMetadata metadata = new DocumentMetadata(clazz, method, null, multiFields);
                            methodSet.add(metadata);
                        }
                    }

                }
            });

            return map;
        }

        @Override
        public Metadata get() {
            return build();
        }
    }


}
