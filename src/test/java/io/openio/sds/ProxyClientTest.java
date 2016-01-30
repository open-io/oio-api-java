package io.openio.sds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import io.openio.sds.exceptions.ContainerNotFoundException;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.http.OioHttp;
import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.OioUrl;

public class ProxyClientTest {

    private static ProxyClient proxy;

    @BeforeClass
    public static void setup() {
        proxy = new ProxyClient(OioHttp.http(new OioHttpSettings()),
                TestHelper.proxySettings());
    }

    @Test
    public void containerNominal() {
        String container = UUID.randomUUID().toString();
        System.out.println(container);
        OioUrl url = OioUrl.url("TEST", container);
        ContainerInfo ci = proxy.createContainer(url);
        assertNotNull(ci);
        assertNotNull(ci.name());
        assertEquals(container, ci.name());
        ci = proxy.getContainerInfo(url);
        System.out.println(proxy.listContainer(url, new ListOptions()));
        System.out.println(ci);
        proxy.deleteContainer(url);
        proxy.getContainerInfo(url);
    }

    @Test(expected = ContainerNotFoundException.class)
    public void getUnknwonContainer() {
        proxy.getContainerInfo(
                OioUrl.url("TEST", UUID.randomUUID().toString()));
    }
}
