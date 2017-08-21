package com.timeyang.jkes.spring.jpa.exception;

import com.timeyang.jkes.core.exception.JkesException;

/**
 * @author chaokunyang
 */
public class NoAvailableRepositoryException extends JkesException {

    public NoAvailableRepositoryException() {
    }

    public NoAvailableRepositoryException(String message) {
        super(message);
    }

    public NoAvailableRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoAvailableRepositoryException(Throwable cause) {
        super(cause);
    }
}
