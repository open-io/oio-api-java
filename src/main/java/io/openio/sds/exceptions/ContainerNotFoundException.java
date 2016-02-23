package io.openio.sds.exceptions;

public class ContainerNotFoundException extends OioException {

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