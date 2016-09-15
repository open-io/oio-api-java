package io.openio.sds;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import io.openio.sds.proxy.ProxySettings;

public class TestHelper {

    private static final String TEST_ACCOUNT = "TEST";
    private static String ns = "OPENIO";
    private static String rawProxyString = "192.168.150.95:6006";
    private static String proxyIp = "192.168.150.95";
    private static int proxyPort = 6006;
    private static String rawEcdString = "192.168.150.23:5000";
    private static boolean isLoaded = false;

    public static ProxySettings proxySettings() {
        if (!isLoaded)
            loadConfiguration();
        return new ProxySettings().url(proxyd()).ns(ns()).ecd(ecd());
    }

    public static String ns() {
        if (!isLoaded)
            loadConfiguration();
        return ns;
    }

    public static String proxyd() {
        if (!isLoaded)
            loadConfiguration();
        return rawProxyString;
    }

    public static String ecd() {
        if (!isLoaded)
            loadConfiguration();
        return rawEcdString;
    }

    public static InetSocketAddress proxyAddr() {
        if (!isLoaded)
            loadConfiguration();
        return new InetSocketAddress(proxyIp, proxyPort);
    }

    public static String testAccount() {
        return TEST_ACCOUNT;
    }

    // TODO provide actual test_file
    public static String test_file() {
        return "/test/file";
    }

    public static byte[] bytes(long size) {
        byte[] res = new byte[(int) size];
        if (size > 0)
            new Random().nextBytes(res);
        return res;
    }

    /**
     * Parse an URL with or without protocol prefix. If there is no protocol,
     * default to "http".
     *
     * @param source
     * @return
     * @throws MalformedURLException
     *             if URL is not parsable, even when adding "http://" prefix
     */
    public static URL parseUnprefixedUrl(String source)
            throws MalformedURLException {
        URL url = null;
        try {
            url = new URL(source);
        } catch (MalformedURLException mue) {
            url = new URL("http://" + source);
        }
        return url;
    }

    /**
     * Load namespace configuration from INI file.
     *
     * @param myNs
     *            name of the namespace to load
     * @param confPath
     *            path to the configuration file
     * @throws FileNotFoundException
     */
    public static void loadConfiguration(String myNs, String confPath)
            throws FileNotFoundException {
        File confFile = new File(confPath);
        if (!confFile.exists() || !confFile.isFile())
            throw new FileNotFoundException(confPath);
        try {
            HierarchicalINIConfiguration conf = new HierarchicalINIConfiguration();
            conf.setDelimiterParsingDisabled(true);
            conf.load(confFile);
            SubnodeConfiguration nsSection = conf.getSection(myNs);
            rawProxyString = nsSection.getString("proxy");
            URL proxyUrl = parseUnprefixedUrl(rawProxyString.split(",")[0]);
            proxyIp = proxyUrl.getHost();
            proxyPort = proxyUrl.getPort();
            rawEcdString = nsSection.getString("ecd");
            ns = myNs;
            isLoaded = true;
        } catch (ConfigurationException e) {
            throw new IllegalArgumentException(e);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Bad proxy or ECD URL in "
                    + confPath, e);
        }
    }

    /**
     * Load configuration from "sds.conf" file found either in "$HOME/.oio/" or
     * "/etc/oio/".
     *
     * @param myNs
     * @throws FileNotFoundException
     */
    public static void loadConfiguration(String myNs)
            throws FileNotFoundException {
        String confPath = System.getProperty("user.home") + File.separator
                + ".oio" + File.separator + "sds.conf";
        try {
            loadConfiguration(myNs, confPath);
        } catch (FileNotFoundException fnfe) {
            try {
                loadConfiguration(myNs, "/etc/oio/sds.conf");
            } catch (FileNotFoundException fnfe2) {
                throw new FileNotFoundException(
                        "Neither /etc/oio/sds.conf nor " + confPath + " exist");
            }
        }
    }

    /**
     * Load configuration from default file for the namespace defined in
     * "OIO_NS" environment variable.
     *
     * @throws FileNotFoundException
     */
    public static void loadConfiguration() {
        String myNs = System.getenv("OIO_NS");
        if (myNs == null || myNs.isEmpty())
            myNs = System.getProperty("OIO_NS");
        if (myNs == null || myNs.isEmpty())
            throw new RuntimeException(
                    "Please define OIO_NS variable to the name of the namespace");
        try {
            loadConfiguration(myNs);
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException(fnfe);
        }
    }
}
