package io.openio.sds;

import java.util.Random;

import io.openio.sds.settings.ProxySettings;

public class TestHelper {

    public static TestHelper INSTANCE;

    private static ProxySettings proxySettings = new ProxySettings()
            .url("http://127.0.0.1:6002")
            .ns("NS");

    public synchronized static TestHelper instance() {
        if (null == INSTANCE) {
            INSTANCE = new TestHelper();
        }
        return INSTANCE;
    }

    public static ProxySettings proxySettings() {
        return proxySettings;
    }

    public String ns() {
        return "NS";
    }

    public String proxyd() {
        return "http://127.0.0.1:6002";
    }

    // TODO provide actual test_file
    public String test_file() {
        return "/test/file";
    }

    public static byte[] bytes(long size) {
        byte[] res = new byte[(int) size];
        if (size > 0)
            new Random().nextBytes(res);
        return res;
    }
}
