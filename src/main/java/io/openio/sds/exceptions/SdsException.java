package io.openio.sds.exceptions;

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