package com.timeyang.jkes.core.metadata;

import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.annotation.DocumentId;
import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.Immutable;
import com.timeyang.jkes.core.annotation.MappedSuperclass;
import com.timeyang.jkes.core.annotation.MultiFields;
import com.timeyang.jkes.core.annotation.Version;
import com.timeyang.jkes.core.support.JkesProperties;
import com.timeyang.jkes.core.util.ClassUtils;
import com.timeyang.jkes.core.util.DocumentUtils;
import com.timeyang.jkes.core.util.ReflectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author chaokunyang
 */
@Immutable
@Named // TODO REMOVE
public final class Metadata {

    private final Set<Class<?>> annotatedDocuments;

    private final Map<Class<?>, DocumentMetadata> documentMetadataMap;

    private Metadata(Set<Class<?>> annotatedDocuments, Map<Class<?>, DocumentMetadata> documentMetadataMap) {
        this.annotatedDocuments = Collections.unmodifiableSet(annotatedDocuments);
        this.documentMetadataMap = Collections.unmodifiableMap(documentMetadataMap);
    }

    // TODO REMOVE
    @Inject
    public Metadata(JkesProperties jkesProperties) {
        Metadata m = new MetadataBuilder(jkesProperties).build();
        this.annotatedDocuments = m.getAnnotatedDocuments();
        this.documentMetadataMap = m.getMetadata();
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
    public Map<Class<?>, DocumentMetadata> getMetadata() {
        return documentMetadataMap;
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

            Map<Class<?>, DocumentMetadata> documentsMetadata = buildDocumentsMetadata(annotatedClasses);

            return new Metadata(annotatedClasses, documentsMetadata);
        }

        private Map<Class<?>, DocumentMetadata> buildDocumentsMetadata(Set<Class<?>> annotatedClasses) {
            Map<Class<?>, DocumentMetadata> map = new HashMap<>();

            annotatedClasses.forEach(clazz -> {
                Set<FieldMetadata> fieldMetadataSet = new HashSet<>();
                Set<MultiFieldsMetadata> multiFieldsMetadataSet = new HashSet<>();
                IdMetadata idMetadata = null;
                VersionMetadata versionMetadata = null;

                Map<String, Method> memberFieldsToCheck = new HashMap<>(); // ensure constant search time

                Class<?> c = clazz;
                do {
                    Method[] methods = clazz.getDeclaredMethods();
                    for(Method method : methods) {
                        if(Modifier.isPublic(method.getModifiers())) {

                            Field field = method.getAnnotation(Field.class);
                            String fieldName = DocumentUtils.getFieldName(method);

                            DocumentId documentId = method.getAnnotation(DocumentId.class);
                            if(documentId != null) {
                                idMetadata = new IdMetadata(method, field, fieldName);
                            }

                            Version version = method.getAnnotation(Version.class);
                            if(version != null) {
                                versionMetadata = new VersionMetadata(method, field, fieldName);
                            }

                            if(field != null) {
                                fieldMetadataSet.add(new FieldMetadata(method, field, fieldName));
                                continue;
                            }else if(documentId != null || version != null) {
                                continue;
                            }

                            MultiFields multiFields = method.getAnnotation(MultiFields.class);
                            if(multiFields != null) {
                                multiFieldsMetadataSet.add(new MultiFieldsMetadata(method,
                                        multiFields, fieldName));
                                continue;
                            }

                            // field and multiFields is null
                            String methodName = method.getName();
                            if(methodName.startsWith("get") || methodName.startsWith("is")) {
                                String memberFieldName = ReflectionUtils.getFieldNameForGetter(methodName);
                                memberFieldsToCheck.put(memberFieldName, method);
                            }
                        }
                    }

                    java.lang.reflect.Field[] declaredFields = c.getDeclaredFields();
                    for(java.lang.reflect.Field memberField : declaredFields) {
                        String memberFieldName = memberField.getName();
                        if(memberFieldsToCheck.containsKey(memberFieldName)) {

                            Method method = memberFieldsToCheck.get(memberFieldName);

                            Field field = memberField.getAnnotation(Field.class);
                            String fieldName = DocumentUtils.getFieldName(method);

                            DocumentId documentId = method.getAnnotation(DocumentId.class);
                            if(documentId != null) {
                                idMetadata = new IdMetadata(method, field, fieldName);
                            }

                            Version version = method.getAnnotation(Version.class);
                            if(version != null) {
                                versionMetadata = new VersionMetadata(method, field, fieldName);
                            }

                            if(field != null) {
                                fieldMetadataSet.add(new FieldMetadata(method, field, fieldName));
                                memberFieldsToCheck.remove(memberFieldName);
                                continue;
                            }else if(documentId != null || version != null) {
                                memberFieldsToCheck.remove(memberFieldName);
                                continue;
                            }

                            MultiFields multiFields = method.getAnnotation(MultiFields.class);
                            if(multiFields != null) {
                                multiFieldsMetadataSet.add(new MultiFieldsMetadata(method,
                                        multiFields, fieldName));

                                memberFieldsToCheck.remove(memberFieldName);
                            }
                        }
                    }

                    c = clazz.getSuperclass();

                    if(!c.isAnnotationPresent(MappedSuperclass.class) &&
                            !c.isAnnotationPresent(javax.persistence.MappedSuperclass.class))
                        break;
                } while (c != Object.class);


                DocumentMetadata documentMetadata = DocumentMetadata.builder()
                        .idMetadata(idMetadata)
                        .versionMetadata(versionMetadata)
                        .fieldMetadataSet(fieldMetadataSet)
                        .multiFieldsMetadataSet(multiFieldsMetadataSet)
                        .build();
                map.put(clazz, documentMetadata);
            });

            return map;
        }

        @Override
        public Metadata get() {
            return build();
        }
    }

}
