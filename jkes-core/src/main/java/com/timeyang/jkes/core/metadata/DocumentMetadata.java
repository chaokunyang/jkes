package com.timeyang.jkes.core.metadata;

import com.timeyang.jkes.core.annotation.Immutable;
import lombok.Builder;

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
        this.fieldMetadataSet = fieldMetadataSet;
        this.multiFieldsMetadataSet = multiFieldsMetadataSet;
        this.idMetadata = idMetadata;
        this.versionMetadata = versionMetadata;
        this.topic = topic;
    }

}
