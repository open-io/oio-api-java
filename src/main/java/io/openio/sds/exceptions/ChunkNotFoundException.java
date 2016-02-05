package io.openio.sds.exceptions;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ChunkNotFoundException extends SdsException {


    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ChunkNotFoundException(String message) {
        super(message);
    }
}
