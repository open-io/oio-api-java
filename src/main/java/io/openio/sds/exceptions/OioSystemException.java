package io.openio.sds.exceptions;

/**
 * Exception for all system / hardware / internal exception in OpenIO
 * @author Christopher Dedeurwaerder
 *
 */
public class OioSystemException extends OioException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public OioSystemException(String message) {
        super(message);
    }

    public OioSystemException(String message, Throwable t) {
        super(message, t);
    }
}
