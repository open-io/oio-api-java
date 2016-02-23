package io.openio.sds.exceptions;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class BadRequestException extends OioException {


    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public BadRequestException(String message) {
        super(message);
    }
}