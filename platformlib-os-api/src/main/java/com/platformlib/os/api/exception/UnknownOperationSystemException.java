package com.platformlib.os.api.exception;

/**
 * Exception to throw when the operation system could not be detected.
 */
public class UnknownOperationSystemException extends OperationSystemException {
    private static final long serialVersionUID = -58622058276593960L;

    /**
     * Message based constructor.
     * @param message message
     */
    public UnknownOperationSystemException(final String message) {
        super(message);
    }
}
