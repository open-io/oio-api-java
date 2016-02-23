package io.openio.sds.exceptions;

public class AccountNotFoundException extends OioException {

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