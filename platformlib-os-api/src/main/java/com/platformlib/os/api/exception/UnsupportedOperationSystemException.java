package com.platformlib.os.api.exception;

/**
 * Exception to throw when the operation system is unsupported.
 */
public class UnsupportedOperationSystemException extends OperationSystemException {
    private static final long serialVersionUID = 4309085787896378346L;

    /**
     * Message based constructor.
     * @param message message
     */
    public UnsupportedOperationSystemException(final String message) {
        super(message);
    }

    /**
     * Throwable based constructor.
     * @param cause cause
     */
    public UnsupportedOperationSystemException(final Throwable cause) {
        super(cause);
    }
}
