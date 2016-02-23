package io.openio.sds.exceptions;

/**
 * This exception is deprecated and will be drop in a future release. Please catch {@link OioException} directly instead.
 */
@Deprecated
public class SdsException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 3270900242235005338L;

    public SdsException(String message) {
        super(message);
    }

    public SdsException(String message, Throwable t) {
        super(message, t);
    }
}