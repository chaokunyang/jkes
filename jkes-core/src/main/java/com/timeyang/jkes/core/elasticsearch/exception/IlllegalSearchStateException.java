package com.timeyang.jkes.core.elasticsearch.exception;

/**
 * @author chaokunyang
 */
public class IlllegalSearchStateException extends IllegalStateException {

    public IlllegalSearchStateException() {
    }

    public IlllegalSearchStateException(String s) {
        super(s);
    }

    public IlllegalSearchStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
