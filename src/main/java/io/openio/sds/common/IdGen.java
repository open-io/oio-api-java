package io.openio.sds.common;

import static io.openio.sds.common.Hex.toHex;

import java.util.Random;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class IdGen {

    private static final int REQ_ID_LENGTH = 16;
    
    private static Random rand = new Random();
    
    /**
     * Generates a new random request id
     * @return the generated id
     */
    public static String requestId() {
        return toHex(bytes(REQ_ID_LENGTH));
    }
    
    private static byte[] bytes(int size){
        byte[] b = new byte[size];
        rand.nextBytes(b);
        return b;
    }
}
