package com.timeyang.jkes.core.exception;

/**
 * Indicate {@link com.timeyang.jkes.core.annotation.DocumentId} and {@link javax.persistence.Id} is not present
 *
 * @author chaokunyang
 */
public class MissingDocumentIdAnnotationException extends JkesException {

    public MissingDocumentIdAnnotationException() {
    }

    public MissingDocumentIdAnnotationException(String message) {
        super(message);
    }

    public MissingDocumentIdAnnotationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingDocumentIdAnnotationException(Throwable cause) {
        super(cause);
    }
}
