package io.openio.sds;

import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.openio.sds.common.Hash;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.OioUrl;
import io.openio.sds.models.Position;
import io.openio.sds.proxy.ProxySettings;

public class TestHelper {

    private static final String TEST_ACCOUNT = "TEST";
    private static String defaultNs = "OPENIO";
    private static boolean isLoaded = false;
    private static Settings settings = null;

    public static ProxySettings proxySettings() {
        return settings().proxy();
    }

    public static Settings settings() {
        if (!isLoaded)
            loadConfiguration();
        return settings;
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
     * Load configuration from "sds.conf" file found either in "$HOME/.oio/" or
     * "/etc/oio/".
     *
     * @param myNs
     *            name of the namespace to load
     * @throws FileNotFoundException
     *             if no configuration file could be found in default places
     */
    public static void loadConfiguration(String myNs) throws FileNotFoundException {
        settings = Settings.forNamespace(myNs);
        isLoaded = true;
    }

    /**
     * Load configuration from default file for the namespace defined in
     * "OIO_NS" environment variable.
     *
     * @throws RuntimeException
     *             if no configuration file could be found in default places
     */
    public static void loadConfiguration() {
        String myNs = System.getenv("OIO_NS");
        if (myNs == null || myNs.isEmpty())
            myNs = System.getProperty("OIO_NS");
        if (myNs == null || myNs.isEmpty())
            myNs = defaultNs;
        try {
            loadConfiguration(myNs);
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException(fnfe);
        }
    }

    public static ObjectInfo newTestObjectInfo(OioUrl url, long size) {
        ObjectInfo info = new ObjectInfo();
        info.url(url);
        info.size(size);

        List<ChunkInfo> l = new ArrayList<ChunkInfo>();
        for (int i = 0; i < 3; i++) {
            String chunkId = Hash.sha256().hashBytes(Integer.toString(i).getBytes()).toString();
            String chunkUrl = format("http://127.0.0.1:601%d/%s", i, chunkId);
            ChunkInfo ci = new ChunkInfo().pos(Position.simple(0)).size(size).url(chunkUrl);
            l.add(ci);
        }
        info.chunks(l);

        return info;
    }

    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int r;
        while ((r = is.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, r);
        }
        return out.toByteArray();
    }
}
