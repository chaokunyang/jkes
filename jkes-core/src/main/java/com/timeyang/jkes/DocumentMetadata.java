package com.timeyang.jkes;

import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.support.JkesProperties;
import com.timeyang.jkes.core.util.ClassUtils;

import javax.inject.Named;
import java.util.Set;

/**
 * @author chaokunyang
 */
@Named
public class DocumentMetadata {

    private final Set<Class<?>> annotatedClasses;

    public DocumentMetadata(JkesProperties jkesProperties) {
        annotatedClasses = ClassUtils.getAnnotatedClasses(jkesProperties.getDocumentBasePackage(), Document.class);
    }

    /**
     * Get set of class annotated with {@link Document}
     * @return
     */
    public Set<Class<?>> getAnnotatedDocumentClasses() {
        return annotatedClasses;
    }
}
