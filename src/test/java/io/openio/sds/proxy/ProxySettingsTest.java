package io.openio.sds.proxy;

import io.openio.sds.exceptions.OioException;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProxySettingsTest {

	@Test
	public void noURLset() {
		ProxySettings settings = new ProxySettings();
		try {
			settings.url();
			fail("Expected OioException");
		} catch (OioException e) {
			assertTrue(e.getMessage().contains("Proxy url not defined"));
		}

		try {
			settings.allHosts();
			fail("Expected OioException");
		} catch (OioException e) {
			assertTrue(e.getMessage().contains("Proxy url not defined"));
		}
	}

	@Test
	public void singleURL() {
		ProxySettings settings = new ProxySettings();
		settings.url("http://127.0.0.1:8080");

		assertEquals(settings.url(), "http://127.0.0.1:8080");

		List<InetSocketAddress> hosts = settings.allHosts();
		assertEquals(hosts.size(), 1);
		InetSocketAddress host = hosts.get(0);
		assertEquals(host.getHostString(), "127.0.0.1");
		assertEquals(host.getPort(), 8080);
	}

	@Test
	public void multipleURLs() {
		ProxySettings settings = new ProxySettings();
		settings.url("http://127.0.0.1:8080,http://127.0.0.1:8081");

		assertEquals(settings.url(), "http://127.0.0.1:8080");

		List<InetSocketAddress> hosts = settings.allHosts();
		assertEquals(hosts.size(), 2);
		InetSocketAddress host0 = hosts.get(0);
		assertEquals(host0.getHostString(), "127.0.0.1");
		assertEquals(host0.getPort(), 8080);

		InetSocketAddress host1 = hosts.get(1);
		assertEquals(host1.getHostString(), "127.0.0.1");
		assertEquals(host1.getPort(), 8081);
	}
}
