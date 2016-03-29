package io.openio.sds.exceptions;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ContainerNotEmptyException extends OioException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ContainerNotEmptyException(String message) {
        super(message);
    }

}
