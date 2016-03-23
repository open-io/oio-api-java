package io.openio.sds;

import io.openio.sds.proxy.ProxySettings;
import io.openio.sds.rawx.RawxSettings;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class Settings {

    private ProxySettings proxy;
    private RawxSettings rawx;
    
    public ProxySettings proxy() {
        return proxy;
    }
    public Settings proxy(ProxySettings proxy) {
        this.proxy = proxy;
        return this;
    }
    public RawxSettings rawx() {
        return rawx;
    }
    public Settings rawx(RawxSettings rawx) {
        this.rawx = rawx;
        return this;
    }    
}