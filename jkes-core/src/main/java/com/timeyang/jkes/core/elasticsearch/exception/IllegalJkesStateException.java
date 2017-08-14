package com.timeyang.jkes.core.elasticsearch.exception;

/**
 * @author chaokunyang
 */
public class IllegalJkesStateException extends IllegalStateException {

    public IllegalJkesStateException() {
    }

    public IllegalJkesStateException(String s) {
        super(s);
    }

    public IllegalJkesStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
