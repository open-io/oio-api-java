package io.openio.sds;

import static io.openio.sds.http.OioHttp.http;

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.URI;

import io.openio.sds.common.SocketProvider;
import io.openio.sds.common.SocketProviders;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.pool.PoolingSettings;
import io.openio.sds.proxy.ProxyClient;
import io.openio.sds.storage.ecd.EcdClient;
import io.openio.sds.storage.rawx.RawxClient;

/**
 * Builder for @link {@link Client} implementations
 * 
 * @author Christopher Dedeurwaerder
 * @author Florent Vennetier
 */
public class ClientBuilder {

	/**
	 * Create a new {@link AdvancedClient} using the specified settings.
	 * 
	 * @param settings
	 *            the settings to use
	 * @return a new {@link AdvancedClient} object
	 */
	public static AdvancedClient newAdvancedClient(Settings settings) {
		OioHttp proxyHttp = http(settings.proxy().http(),
		        proxySocketProvider(settings.proxy().url(),
		                settings.proxy().http(), settings.proxy().pooling()));
		OioHttp rawxHttp = http(settings.rawx().http(),
		        rawxSocketProvider(settings.rawx().http()));
		ProxyClient proxy = new ProxyClient(proxyHttp, settings.proxy());
		RawxClient rawx = new RawxClient(rawxHttp, settings.rawx());
		EcdClient ecd = null == settings.proxy().ecd() 
				? null
				: new EcdClient(rawxHttp, settings.rawx(), settings.proxy().allEcdHosts());
		return new DefaultClient(proxy, rawx, ecd);
	}

	/**
	 * Create an OpenIO SDS client using the specified settings.
	 *
	 * @param settings
	 *            the settings to use
	 * @return a new {@link Client} object
	 */
	public static Client newClient(Settings settings) {
	    return newAdvancedClient(settings);
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
	public static Client newClient(String ns, String proxydUrl) {
		Settings settings = new Settings();
		settings.proxy().url(proxydUrl).ns(ns);
		return newClient(settings);
	}

	/**
	 * Create a client for the specified OpenIO SDS namespace. Load the settings from the default
	 * places ("$HOME/.oio/sds.conf" or "/etc/oio/sds.conf.d/$NS" or "/etc/oio/sds.conf").
	 *
	 * @param ns the OpenIO Namespace to connect to
	 * @return a new {@link Client} object
	 * @throws FileNotFoundException
	 *            if no configuration file for the specified namespace has been found
	 */
	public static Client newClient(String ns) throws FileNotFoundException {
	    return newClient(Settings.forNamespace(ns));
	}

	/**
	 * Creates a client without specific configuration. Useful for testing
	 * purpose
	 *
	 * @param ns
	 *            the OpenIO Namespace
	 * @param proxydUrl
	 *            the url of OpenIO proxyd service
	 * @param ecdUrl 
	 *            url of ECD service to manage Erasure Coding
	 * @return The build {@link Client}
	 */
	public static Client newClient(String ns,
			String proxydUrl,
			String ecdUrl) {
		Settings settings = new Settings();
		settings.proxy().url(proxydUrl).ns(ns);
		settings.proxy().ecd(ecdUrl);
		return newClient(settings);
	}

	private static SocketProvider proxySocketProvider(String url,
	        final OioHttpSettings http, PoolingSettings pooling) {
		URI uri = URI.create(url);
		InetSocketAddress target = new InetSocketAddress(uri.getHost(),
		        uri.getPort());
		return pooling.enabled()
		        ? SocketProviders.pooledSocketProvider(pooling, http, target)
		        : SocketProviders.directSocketProvider(http);
	}

	private static SocketProvider rawxSocketProvider(
	        final OioHttpSettings http) {
		return SocketProviders.directSocketProvider(http);
	}
}
