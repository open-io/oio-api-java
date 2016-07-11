package io.openio.sds;

import io.openio.sds.proxy.ProxySettings;
import io.openio.sds.storage.rawx.RawxSettings;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class Settings {

    private ProxySettings proxy = new ProxySettings();
    private RawxSettings rawx = new RawxSettings();

    /**
     * Returns oio proxyd connection configuration
     * @return oio proxyd connection configuration
     */
    public ProxySettings proxy() {
        return proxy;
    }
    
    /**
     * Specifies a proxyd connection configuration
     */
    public Settings proxy(ProxySettings proxy) {
        this.proxy = proxy;
        return this;
    }
    
    /**
     * Returns rawx services connection configuration
     * @return rawx services connection configuration
     */
    public RawxSettings rawx() {
        return rawx;
    }
    
    /**
     * 
     * @param rawx
     * @return
     */
    public Settings rawx(RawxSettings rawx) {
        this.rawx = rawx;
        return this;
    }    
}