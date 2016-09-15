package io.openio.sds;

import io.openio.sds.proxy.ProxySettings;
import io.openio.sds.storage.rawx.RawxSettings;

/**
 * 
 * @author Christopher Dedeurwaerder
 * @author Florent Vennetier
 */
public class Settings {

    public static String MULTI_VALUE_SEPARATOR = ",";

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
     * @param proxy
     *  the configuration to set
     * @return this
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
     * Specifies rawx connections configuration
     * @param rawx the configuration to set
     * @return this
     */
    public Settings rawx(RawxSettings rawx) {
        this.rawx = rawx;
        return this;
    }
}