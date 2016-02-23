package io.openio.sds;

import static io.openio.sds.common.Check.checkArgument;

import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.settings.ProxySettings;
import io.openio.sds.settings.RawxSettings;
import io.openio.sds.settings.Settings;


/**
 * Builder for @link {@link Client} implementations
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ClientBuilder {

    private String ns;
    private String proxydUrl;
    private OioHttp http;

    /**
     * Generates a client builder to prepare {@link Client} configuration
     * 
     * @return a new {@code ClientBuilder}
     */
    public static ClientBuilder prepareClient() {
        return new ClientBuilder();
    }

    /**
     * Defines the url of the OpenIO proxyd service
     * 
     * @param proxydUrl
     *            the url to set
     * @return this
     */
    public ClientBuilder proxydUrl(String proxydUrl) {
        this.proxydUrl = proxydUrl;
        return this;
    }

    /**
     * Defines the OpenIO Namespace
     *
     * @param ns
     *            the OpenIO Namespace to set
     * @return this
     */
    public ClientBuilder ns(String ns) {
        this.ns = ns;
        return this;
    }

    /**
     * Set a specific {@link OioHttp} instance to be used by the built
     * clients
     * 
     * @param http
     *            the OioHttp instance to set
     * @return this
     */
    public ClientBuilder http(OioHttp http) {
        this.http = http;
        return this;
    }

    /**
     * Builds a client using the specified settings
     * 
     * @return the new client
     */
    public DefaultClient build() {
        checkArgument(null != ns, "Namespace cannot be null");
        checkArgument(null != proxydUrl, "Proxyd URL cannot be null");
        return new DefaultClient(
                null == http ? OioHttp.http(new OioHttpSettings()) : http,
                new Settings().proxy(new ProxySettings()
                        .ns(ns)
                        .url(proxydUrl))
                        .rawx(new RawxSettings()));
    }

    /**
     * Creates a client without specific configuration. Useful for testing
     * purpose
     *
     * @param ns
     *            the OpenIO Namespace
     * @param proxydUrl
     *            the url of OpenIO proxyd service
     * @return The build {@link Client}
     */
    public static DefaultClient newClient(String ns, String proxydUrl) {
        return new DefaultClient(OioHttp.http(new OioHttpSettings()),
                new Settings().proxy(new ProxySettings()
                        .ns(ns)
                        .url(proxydUrl))
                        .rawx(new RawxSettings()));
    }
}
