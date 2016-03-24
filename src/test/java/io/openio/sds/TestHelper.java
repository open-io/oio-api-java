package io.openio.sds;

import java.net.InetSocketAddress;
import java.util.Random;

import io.openio.sds.proxy.ProxySettings;

public class TestHelper {

	private static final String TEST_ACCOUNT = "TEST";
	private static String proxyIp = "127.0.0.1";
	private static int proxyPort = 6002;

	public static TestHelper INSTANCE;

	private static ProxySettings proxySettings = new ProxySettings()
			.url(proxyd())
			.ns(ns());

	public synchronized static TestHelper instance() {
		if (null == INSTANCE) {
			INSTANCE = new TestHelper();
		}
		return INSTANCE;
	}

	public static ProxySettings proxySettings() {
		return proxySettings;
	}

	public static String ns() {
		return "NS";
	}

	public static String proxyd() {
		return String.format("http://%s:%d", proxyIp, proxyPort);
	}

	public static InetSocketAddress proxyAddr() {
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
}
