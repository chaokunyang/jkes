package com.timeyang.jkes.core.metadata;

import com.timeyang.jkes.core.annotation.Immutable;
import lombok.Builder;

import java.util.Collections;
import java.util.Set;

/**
 * DocumentMetadata
 *
 * @author chaokunyang
 */
@Immutable
@Builder
public final class DocumentMetadata {

    private final Class<?> clazz;
    private final Set<FieldMetadata> fieldMetadataSet;
    private final Set<MultiFieldsMetadata> multiFieldsMetadataSet;

    private final IdMetadata idMetadata;
    private final VersionMetadata versionMetadata;

    private final String topic;

    public DocumentMetadata(
            Class<?> clazz,
            Set<FieldMetadata> fieldMetadataSet,
            Set<MultiFieldsMetadata> multiFieldsMetadataSet,
            IdMetadata idMetadata,
            VersionMetadata versionMetadata,
            String topic) {
        this.clazz = clazz;
        this.fieldMetadataSet = Collections.unmodifiableSet(fieldMetadataSet);
        this.multiFieldsMetadataSet = Collections.unmodifiableSet(multiFieldsMetadataSet);
        this.idMetadata = idMetadata;
        this.versionMetadata = versionMetadata;
        this.topic = topic;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Set<FieldMetadata> getFieldMetadataSet() {
        return fieldMetadataSet;
    }

    public Set<MultiFieldsMetadata> getMultiFieldsMetadataSet() {
        return multiFieldsMetadataSet;
    }

    public IdMetadata getIdMetadata() {
        return idMetadata;
    }

    public VersionMetadata getVersionMetadata() {
        return versionMetadata;
    }

    public String getTopic() {
        return topic;
    }
}
