package com.timeyang.jkes.core.metadata;

import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.annotation.DocumentId;
import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.Immutable;
import com.timeyang.jkes.core.annotation.MappedSuperclass;
import com.timeyang.jkes.core.annotation.MultiFields;
import com.timeyang.jkes.core.annotation.Version;
import com.timeyang.jkes.core.elasticsearch.exception.IllegalJkesStateException;
import com.timeyang.jkes.core.exception.MissingDocumentIdAnnotationException;
import com.timeyang.jkes.core.kafka.util.KafkaUtils;
import com.timeyang.jkes.core.support.JkesProperties;
import com.timeyang.jkes.core.util.ClassUtils;
import com.timeyang.jkes.core.util.DocumentUtils;
import com.timeyang.jkes.core.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.persistence.Id;
import java.lang.reflect.AccessibleObject;
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

    private volatile static Metadata metadata; // use volatile to ensure safely publish

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
        this.documentMetadataMap = m.getMetadataMap();
    }

    @PostConstruct
    public void init() {
        metadata = this;
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
    public Map<Class<?>, DocumentMetadata> getMetadataMap() {
        return documentMetadataMap;
    }

    /**
     * If metadata is null, this method will throw IllegalJkesStateException.
     * <p>So there is no need to check whether metadata is null in the caller</p>
     *
     * @return Metadata
     * @throws IllegalJkesStateException throw IllegalJkesStateException when metadata is null
     */
    public static Metadata getMetadata() {
        if(metadata == null)
            throw new IllegalJkesStateException("Metadata doesn't be inited correctly, the init method doesn't get invoked");
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

            Map<Class<?>, DocumentMetadata> documentsMetadata = buildDocumentsMetadata(annotatedClasses);

            return new Metadata(annotatedClasses, documentsMetadata);
        }

        private Map<Class<?>, DocumentMetadata> buildDocumentsMetadata(Set<Class<?>> annotatedClasses) {
            Map<Class<?>, DocumentMetadata> map = new HashMap<>();

            annotatedClasses.forEach(clazz -> {
                DocumentMetadata documentMetadata =
                        new DocumentMetadataBuilder().buildDocumentMetadata(clazz);

                map.put(clazz, documentMetadata);
            });

            return map;
        }


        @Override
        public Metadata get() {
            return build();
        }
    }

    private static class DocumentMetadataBuilder {
        private Set<FieldMetadata> fieldMetadataSet;
        private Set<MultiFieldsMetadata> multiFieldsMetadataSet;
        private IdMetadata idMetadata = null;
        private VersionMetadata versionMetadata = null;

        private DocumentMetadataBuilder() {
            this.fieldMetadataSet = new HashSet<>();
            this.multiFieldsMetadataSet = new HashSet<>();
        }

        private DocumentMetadata buildDocumentMetadata(Class<?> clazz) {
            Map<String, Method> memberFieldsToCheck = new HashMap<>(); // ensure constant search time

            Class<?> c = clazz;
            do {
                handleMethods(c, memberFieldsToCheck);

                handleFields(c, memberFieldsToCheck);

                c = c.getSuperclass();

                if(!c.isAnnotationPresent(MappedSuperclass.class) &&
                        !c.isAnnotationPresent(javax.persistence.MappedSuperclass.class))
                    break;
            } while (c != Object.class);

            if(this.idMetadata == null)
                throw new MissingDocumentIdAnnotationException(
                        "Document id is not present, " +
                                DocumentId.class + " or " + Id.class + " must be present!");

            String topic = KafkaUtils.getTopic(clazz);

            return DocumentMetadata.builder()
                    .idMetadata(this.idMetadata)
                    .versionMetadata(this.versionMetadata)
                    .fieldMetadataSet(this.fieldMetadataSet)
                    .multiFieldsMetadataSet(this.multiFieldsMetadataSet)
                    .topic(topic)
                    .build();
        }

        private void handleMethods(Class<?> clazz, Map<String, Method> memberFieldsToCheck) {
            Method[] methods = clazz.getDeclaredMethods();
            for(Method method : methods) {
                if(Modifier.isPublic(method.getModifiers())) {

                    boolean annotated = handleMember(method, method);

                    // not annotated
                    if(!annotated) {
                        String methodName = method.getName();
                        if(methodName.startsWith("get") || methodName.startsWith("is")) {
                            String memberFieldName = ReflectionUtils.getFieldNameForGetter(methodName);
                            memberFieldsToCheck.put(memberFieldName, method);
                        }
                    }
                }
            }
        }

        private void handleFields(Class<?> c, Map<String, Method> memberFieldsToCheck) {
            java.lang.reflect.Field[] declaredFields = c.getDeclaredFields();
            for(java.lang.reflect.Field memberField : declaredFields) {
                String memberFieldName = memberField.getName();
                if(memberFieldsToCheck.containsKey(memberFieldName)) {

                    Method method = memberFieldsToCheck.get(memberFieldName);

                    handleMember(memberField, method);
                }
            }
        }

        /**
         * If member is annotated, return true, else return false.
         * <p>If member is annotated and corresponding metadata is not filled, fill the metadata</p>
         *
         * @param member member to check annotation
         * @param method method
         * @return whether member is annotated
         */
        private boolean handleMember(AccessibleObject member, Method method) {
            boolean annotated = false;

            Field field = member.getAnnotation(Field.class);
            if(field != null) {
                annotated = true;
                String fieldName = DocumentUtils.getFieldName(method);
                FieldMetadata fieldMetadata = new FieldMetadata(method, field, fieldName);
                if(!this.fieldMetadataSet.contains(fieldMetadata)) {
                    this.fieldMetadataSet.add(fieldMetadata);
                }
            }

            MultiFields multiFields = member.getAnnotation(MultiFields.class);
            if(multiFields != null) {
                annotated = true;
                String fieldName = DocumentUtils.getFieldName(method);
                MultiFieldsMetadata multiFieldsMetadata =
                        new MultiFieldsMetadata(method, multiFields, fieldName);
                if(!this.multiFieldsMetadataSet.contains(multiFieldsMetadata))
                    this.multiFieldsMetadataSet.add(multiFieldsMetadata);
            }

            if(member.isAnnotationPresent(DocumentId.class) || member.isAnnotationPresent(Id.class)) {
                annotated = true;
                if(this.idMetadata == null)
                    this.idMetadata = new IdMetadata(method, field, DocumentUtils.getFieldName(method));
            }

            if(member.isAnnotationPresent(Version.class)
                    || member.isAnnotationPresent(javax.persistence.Version.class)) {
                annotated = true;
                if(this.versionMetadata == null)
                    this.versionMetadata =
                            new VersionMetadata(method, field, DocumentUtils.getFieldName(method));
            }

            return annotated;
        }
    }
}
