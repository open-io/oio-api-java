package io.openio.sds.exceptions;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ContainerExistException extends OioException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ContainerExistException(String message) {
        super(message);
    }
}