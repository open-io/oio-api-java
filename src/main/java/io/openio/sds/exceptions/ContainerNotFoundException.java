package io.openio.sds.exceptions;

public class ContainerNotFoundException extends SdsException {

    /**
     * 
     */
    private static final long serialVersionUID = -4662162990209727136L;
    
    public ContainerNotFoundException() {
        super(null);
    }
    public ContainerNotFoundException(String message) {
        super(message);
    }
}