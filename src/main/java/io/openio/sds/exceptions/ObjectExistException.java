package io.openio.sds.exceptions;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ObjectExistException extends OioException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ObjectExistException(String message) {
        super(message);
    }

}