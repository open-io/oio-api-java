package io.openio.sds.settings;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ProxySettings {

    private String url;
    private String ns;

    public ProxySettings() {
    }

    public String url() {
        return url;
    }

    public ProxySettings url(String url) {
        this.url = url;
        return this;
    }

    public String ns() {
        return ns;
    }

    public ProxySettings ns(String ns) {
        this.ns = ns;
        return this;
    }
}