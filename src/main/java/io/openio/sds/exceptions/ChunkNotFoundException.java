package io.openio.sds.exceptions;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ChunkNotFoundException extends OioException {


    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ChunkNotFoundException(String message) {
        super(message);
    }
}
