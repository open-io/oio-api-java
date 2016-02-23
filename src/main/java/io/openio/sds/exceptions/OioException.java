package io.openio.sds.exceptions;

/**
 * Base exception for OpenIO exceptions
 * 
 * @author Christopher Dedeurwaerder
 *
 */
@SuppressWarnings("deprecation")
public class OioException extends SdsException {

    /**
     * 
     */
    private static final long serialVersionUID = 3270900242235005338L;

    public OioException(String message) {
        super(message);
    }

    public OioException(String message, Throwable t) {
        super(message, t);
    }
}