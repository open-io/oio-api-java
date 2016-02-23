package io.openio.sds.exceptions;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ReferenceAlreadyExistException extends OioException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ReferenceAlreadyExistException(String message) {
        super(message);
    }

}
