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
    private String url;
    private ArrayList<InetSocketAddress> hosts = new ArrayList<InetSocketAddress>();
    private String ecd;
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
        if (null == this.url) {
            throw new OioException("Proxy url not defined");
        } else {
        return String.format("http://%1$s:%2$d",
                hosts.get(0).getHostString(), hosts.get(0).getPort());
        }
    }

    public ProxySettings url(String urlv) {
        this.url = urlv;
        this.hosts = strToSocketAddressList(this.url);
        return this;
    }

    /**
     * @return a read-only list of all known proxy hosts
     */
    public List<InetSocketAddress> allHosts() {
        if (null == this.url) {
            throw new OioException("Proxy url not defined");
        } else {
            return Collections.unmodifiableList(this.hosts);
        }
    }

    public String ns() {
        if (null != ns) {
            return ns;
        }
        throw new OioException("Namespace not defined");
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
        if (ecdHosts == null || ecdHosts.size() <= 0)
            return null;
        return String.format("http://%1$s:%2$d",
                ecdHosts.get(0).getHostString(), ecdHosts.get(0).getPort());
    }

    public ProxySettings ecd(String ecdv) {
        if (ecdv != null) {
            this.ecd = ecdv;
            this.ecdHosts = strToSocketAddressList(this.ecd);
        }
        return this;
    }

    /**
     * @return a read-only list of all known ECD hosts
     */
    public List<InetSocketAddress> allEcdHosts() {
        if ((this.ecdHosts == null || this.ecdHosts.size() == 0) && this.ecd != null)
            this.ecdHosts = strToSocketAddressList(this.ecd);
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
