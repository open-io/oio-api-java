package io.openio.sds.settings;

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