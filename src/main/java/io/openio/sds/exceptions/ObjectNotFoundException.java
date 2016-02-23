package io.openio.sds.exceptions;

public class ObjectNotFoundException extends OioException {

    /**
     * 
     */
    private static final long serialVersionUID = -2995680181164449256L;

    public ObjectNotFoundException() {
        super(null);
    }

    public ObjectNotFoundException(String message) {
        super(message);
    }
}