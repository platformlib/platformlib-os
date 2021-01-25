package com.platformlib.os.api.exception;

/**
 * Base exception.
 */
public class OperationSystemException extends RuntimeException {
    private static final long serialVersionUID = 535859533289567028L;

    /**
     * Message only based constructor.
     * @param message message
     */
    public OperationSystemException(final String message) {
        super(message);
    }

    /**
     * Exception only based constructor
     * @param cause throwable
     */
    public OperationSystemException(final Throwable cause) {
        super(cause);
    }
}
