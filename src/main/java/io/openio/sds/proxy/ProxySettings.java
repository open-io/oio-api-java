package io.openio.sds.proxy;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.openio.sds.Settings;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.pool.PoolingSettings;

/**
 * 
 * @author Christopher Dedeurwaerder
 * @author Florent Vennetier
 */
public class ProxySettings {

    private String ns;
    private ArrayList<InetSocketAddress> hosts = new ArrayList<InetSocketAddress>();
    private ArrayList<InetSocketAddress> ecdHosts = new ArrayList<InetSocketAddress>();
    private boolean ecdrain = true; // not configurable atm cuz we can't do
                                    // somehow else
    private boolean autocreate = true;
    private OioHttpSettings http = new OioHttpSettings();
    private PoolingSettings pooling = new PoolingSettings();

    public ProxySettings() {
    }

    /**
     * Convert a comma-separated list of service addresses to a list of
     * {@link InetSocketAddress}.
     *
     * @param urlv
     *            a comma-separated list of service addresses
     * @return a list of {@link InetSocketAddress}
     */
    public static ArrayList<InetSocketAddress> strToSocketAddressList(String urlv) {
        ArrayList<InetSocketAddress> addrs = new ArrayList<InetSocketAddress>();
        for (String url : urlv.split(Settings.MULTI_VALUE_SEPARATOR)) {
            String uriBase;
            if (!url.contains("://"))
                uriBase = "http://" + url;
            else
                uriBase = url;
            try {
                URI uri = new URI(uriBase);
                addrs.add(new InetSocketAddress(uri.getHost(), uri.getPort()));
            } catch (URISyntaxException e) {
                throw new OioException("Could not parse [" + "" + "] as a URI", e);
            }
        }
        return addrs;
    }

    /**
     * @return The first proxy URL
     */
    public String url() {
        return String.format("http://%1$s:%2$d",
                hosts.get(0).getHostString(), hosts.get(0).getPort());
    }

    public ProxySettings url(String urlv) {
        hosts = strToSocketAddressList(urlv);
        return this;
    }

    /**
     * @return a read-only list of all known proxy hosts
     */
    public List<InetSocketAddress> allHosts() {
        return Collections.unmodifiableList(this.hosts);
    }

    public String ns() {
        return ns;
    }

    public ProxySettings ns(String ns) {
        this.ns = ns;
        return this;
    }

    public OioHttpSettings http() {
        return http;
    }

    public ProxySettings http(OioHttpSettings http) {
        this.http = http;
        return this;
    }

    public PoolingSettings pooling() {
        return pooling;
    }

    public ProxySettings pooling(PoolingSettings pooling) {
        this.pooling = pooling;
        return this;
    }

    /**
     * @return the first ECD URL
     */
    public String ecd() {
        if (null == ecdHosts || 0 >= ecdHosts.size())
            return null;
        return String.format("http://%1$s:%2$d",
                ecdHosts.get(0).getHostString(), ecdHosts.get(0).getPort());
    }

    public ProxySettings ecd(String ecdv) {
        if (null != ecdv)
            this.ecdHosts = strToSocketAddressList(ecdv);
        return this;
    }

    /**
     * @return a read-only list of all known ECD hosts
     */
    public List<InetSocketAddress> allEcdHosts() {
        return Collections.unmodifiableList(this.ecdHosts);
    }

    public boolean ecdrain() {
        return ecdrain;
    }

    public boolean autocreate() {
        return autocreate;
    }

    public ProxySettings autocreate(boolean autocreate) {
        this.autocreate = autocreate;
        return this;
    }
}
