package io.openio.sds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.openio.sds.common.IniFile;
import io.openio.sds.proxy.ProxySettings;
import io.openio.sds.storage.rawx.RawxSettings;

/**
 * 
 * @author Christopher Dedeurwaerder
 * @author Florent Vennetier
 */
public class Settings {

    /**
     * Separator used in parameters with multiple values.
     */
    public static final String MULTI_VALUE_SEPARATOR = ",";

    private ProxySettings proxy = new ProxySettings();
    private RawxSettings rawx = new RawxSettings();

    /**
     * Load namespace settings from INI file.
     *
     * @param myNs
     *            name of the namespace to load
     * @param confPath
     *            path to the configuration file
     * @return a new {@link Settings} object
     * @throws FileNotFoundException
     *             when the specified file does not exist
     * @throws IllegalArgumentException
     *             when a parsing error occurs
     */
    public static Settings fromFile(String myNs, String confPath) throws FileNotFoundException {
        try {
            IniFile conf1 = new IniFile(confPath);
            if (!conf1.hasSection(myNs)) {
                throw new FileNotFoundException(
                        "Section [" + myNs + "] does not exist or is empty");
            }
            ProxySettings pst = new ProxySettings().ns(myNs);
            String rawProxyString = conf1.getString(myNs, "proxy", null);
            if (rawProxyString == null)
                throw new IllegalArgumentException("No proxy address found in configuration");
            pst.url(rawProxyString);
            String rawEcdString = conf1.getString(myNs, "ecd", null);
            pst.ecd(rawEcdString);  // can be null
            // TODO: load rawx settings somehow
            return new Settings().proxy(pst).rawx(new RawxSettings());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read " + confPath);
        }
    }

    /**
     * Load configuration from "sds.conf" file found either in "$HOME/.oio/" or
     * "/etc/oio/", or from "/etc/oio/sds.conf.d/$NS".
     *
     * @param myNs
     *            name of the namespace to load
     * @return a new {@link Settings} object
     * @throws FileNotFoundException
     *             if no valid configuration file could be found in default places
     */
    public static Settings forNamespace(String myNs) throws FileNotFoundException {
        String confPath = System.getProperty("user.home") + File.separator + ".oio" + File.separator
                + "sds.conf";
        String confPath2 = "/etc/oio/sds.conf.d/" + myNs;
        String confPath3 = "/etc/oio/sds.conf";
        try {
            return fromFile(myNs, confPath);
        } catch (FileNotFoundException fnfe) {
            try {
                return fromFile(myNs, confPath2);
            } catch (FileNotFoundException fnfe2) {
                try {
                    return fromFile(myNs, confPath3);
                } catch (FileNotFoundException fnfe3) {
                    throw new FileNotFoundException("None of " + confPath3 + ", " + confPath2
                            + " or " + confPath + " contains a valid configuration for " + myNs);
                }
            }
        }
    }

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
