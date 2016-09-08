package io.openio.sds;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttp.RequestBuilder;
import io.openio.sds.http.Verifiers;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.ObjectList;
import io.openio.sds.models.OioUrl;
import io.openio.sds.proxy.ProxyClient;
import io.openio.sds.proxy.ProxySettings;

public class ProxyClientTest {

    @Test(expected = IllegalArgumentException.class)
    public void listContainerNullUrl() {

        OioHttp mockedHttp = Mockito.mock(OioHttp.class);
        ProxySettings mockedSettings = Mockito.mock(ProxySettings.class);

        ProxyClient proxy = new ProxyClient(mockedHttp, mockedSettings);
        proxy.listContainer(null, new ListOptions());

    }

    @Test(expected = IllegalArgumentException.class)
    public void listContainerNullListOptions() {

        OioHttp mockedHttp = Mockito.mock(OioHttp.class);
        ProxySettings mockedSettings = Mockito.mock(ProxySettings.class);

        ProxyClient proxy = new ProxyClient(mockedHttp, mockedSettings);

        OioUrl url = Mockito.mock(OioUrl.class);
        proxy.listContainer(url, null);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void listContainerOk() {
        ProxySettings mockedSettings = Mockito.mock(ProxySettings.class);
        ObjectList mockedList = Mockito.mock(ObjectList.class);

        RequestBuilder mockedBuilder = Mockito.mock(RequestBuilder.class);
        Mockito.when(
                mockedBuilder.query(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(mockedBuilder);
        Mockito.when(
                mockedBuilder.header(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(mockedBuilder);
        Mockito.when(mockedBuilder.verifier(Verifiers.CONTAINER_VERIFIER))
                .thenReturn(mockedBuilder);
        Mockito.when(mockedBuilder.execute((Class<ObjectList>) Mockito.any()))
                .thenReturn(mockedList);

        OioHttp mockedHttp = Mockito.mock(OioHttp.class);
        Mockito.when(mockedHttp.get(Mockito.anyString()))
                .thenReturn(mockedBuilder);

        ProxyClient proxy = new ProxyClient(mockedHttp, mockedSettings);

        OioUrl url = Mockito.mock(OioUrl.class);
        Mockito.when(url.account()).thenReturn("account");
        Mockito.when(url.container()).thenReturn("container_name");
        Mockito.when(url.object()).thenReturn("object_name");

        Assert.assertNotNull(proxy.listContainer(url, new ListOptions()));

    }

}
