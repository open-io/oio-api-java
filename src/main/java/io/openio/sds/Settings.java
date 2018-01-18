package io.openio.sds;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

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
     * Load namespace settings from INI file.
     *
     * @param myNs
     *            name of the namespace to load
     * @param confPath
     *            path to the configuration file
     * @return a new {@link Settings} object
     * @throws FileNotFoundException
     *             when the specified file does not exist
     */
    public static Settings fromFile(String myNs, String confPath) throws FileNotFoundException {
        File confFile = new File(confPath);
        if (!confFile.exists() || !confFile.isFile())
            throw new FileNotFoundException(confPath);
        try {
            HierarchicalINIConfiguration conf = new HierarchicalINIConfiguration();
            conf.setDelimiterParsingDisabled(true);
            conf.load(confFile);
            SubnodeConfiguration nsSection = conf.getSection(myNs);
            if (nsSection.isEmpty()) {
                throw new FileNotFoundException();
            }
            String rawProxyString = nsSection.getString("proxy");
            String rawEcdString = nsSection.getString("ecd");
            ProxySettings pst = new ProxySettings().ns(myNs).url(rawProxyString).ecd(rawEcdString);
            // TODO: load rawx settings somehow
            return new Settings().proxy(pst).rawx(new RawxSettings());
        } catch (ConfigurationException e) {
            throw new IllegalArgumentException(e);
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
     *             if no configuration file could be found in default places
     */
    public static Settings forNamespace(String myNs) throws FileNotFoundException {
        String confPath = System.getProperty("user.home") + File.separator + ".oio" + File.separator
                + "sds.conf";
        try {
            return fromFile(myNs, confPath);
        } catch (FileNotFoundException fnfe) {
            try {
                return fromFile(myNs, "/etc/oio/sds.conf.d/" + myNs);
            } catch (FileNotFoundException fnfe2) {
                try {
                    return fromFile(myNs, "/etc/oio/sds.conf");
                } catch (FileNotFoundException fnfe3) {
                    throw new FileNotFoundException(
                            "Neither /etc/oio/sds.conf nor " + confPath + " exist");
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