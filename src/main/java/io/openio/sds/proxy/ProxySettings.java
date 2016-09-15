package io.openio.sds.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.openio.sds.Settings;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.pool.PoolingSettings;

/**
 * 
 * @author Christopher Dedeurwaerder
 * @author Florent Vennetier
 */
public class ProxySettings {

    private String ns;
    private ArrayList<String> urls = new ArrayList<String>();
    private ArrayList<String> ecds = new ArrayList<String>();
    private boolean ecdrain = true; //not configurable atm cuz we can't do somehow else
    private boolean autocreate = true;
    private OioHttpSettings http = new OioHttpSettings();
    private PoolingSettings pooling = new PoolingSettings();

    public ProxySettings() {
    }

    /**
     * @return The first proxy URL
     */
    public String url() {
        return urls.get(0);
    }

    public ProxySettings url(String urlv) {
        for (String url: urlv.split(Settings.MULTI_VALUE_SEPARATOR)) {
            if (!url.contains("://"))
                this.urls.add("http://" + url);
            else
                this.urls.add(url);
        }
        return this;
    }

    /**
     * @return a read-only list of all known proxy URLs
     */
    public List<String> allUrls() {
        return Collections.unmodifiableList(this.urls);
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
        return ecds.get(0);
    }

    public ProxySettings ecd(String ecdv) {
        for (String url: ecdv.split(Settings.MULTI_VALUE_SEPARATOR))
            if (!url.contains("://"))
                this.ecds.add("http://" + url);
            else
                this.ecds.add(url);
        return this;
    }

    /**
     * @return a read-only list of all known ECD URLs
     */
    public List<String> allEcds() {
        return Collections.unmodifiableList(this.ecds);
    }

    public boolean ecdrain(){
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
