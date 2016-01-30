package io.openio.sds;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import io.openio.sds.http.HttpSettings;
import io.openio.sds.http.OioHttp;
import io.openio.sds.models.OioUrl;

public class ProxyClientTest {

    private static ProxyClient proxy;

    @BeforeClass
    public static void setup() {
        proxy = new ProxyClient(OioHttp.http(new HttpSettings()),
                TestHelper.proxySettings());
    }

    @Test
    public void containerNominal() {
        String container = UUID.randomUUID().toString();
        System.out.println(container);
        proxy.createContainer(OioUrl.url("TEST", container));
        
    }
}
