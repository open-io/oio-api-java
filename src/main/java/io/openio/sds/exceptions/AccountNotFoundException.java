package io.openio.sds.exceptions;

public class AccountNotFoundException extends SdsException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public AccountNotFoundException() {
        super(null);
    }
    public AccountNotFoundException(String message) {
        super(message);
    }
}