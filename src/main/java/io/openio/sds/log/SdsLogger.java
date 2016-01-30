package io.openio.sds.log;

public abstract class SdsLogger {

    private static int loaded = -1;
    
    public static SdsLogger getLogger(Class<?> c){
        ensureLoaded();
        return null;
    }

    private static void ensureLoaded() {
       if(-1 == loaded) {
       }
    }
    
    
    
}
