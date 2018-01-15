package io.openio.sds.proxy;

import io.openio.sds.RequestContext;
import io.openio.sds.TestSocketProvider;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.ObjectList;
import io.openio.sds.models.OioUrl;
import io.openio.sds.models.ReferenceInfo;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.openio.sds.common.IdGen.requestId;
import static io.openio.sds.common.JsonUtils.gson;
import static io.openio.sds.common.OioConstants.LIST_TRUNCATED_HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProxyClientTest {

    String NAMESPACE = "OPENIO";
    String ACCOUNT_NAME = "testaccount";
    String CONTAINER_NAME = "testcontainer";
    String OBJECT_NAME = "testobject";

    class FixedTimeoutRequestContext extends RequestContext {
        @Override
        public int timeout() {
            return this.timeout;
        }
    }

    void verifyRequest(TestSocketProvider socketProvider, String method, String path, String data,
            RequestContext reqCtx) {
        List<ByteArrayOutputStream> outputs = socketProvider.outputs();
        assertEquals(outputs.size(), 1);
        ByteArrayOutputStream output = outputs.get(0);

        String expectedOutput = method + " " + path + " HTTP/1.1\r\n" + "X-oio-req-id: "
                + reqCtx.requestId() + "\r\n" + "Accept: */*\r\n" + "Connection: close\r\n"
                + "User-Agent: oio-http\r\n" + "X-oio-timeout: "
                + OioHttp.timeoutMillisToStringMicros(reqCtx.timeout()) + "\r\n"
                + "Host: 127.0.0.1:8080\r\n" + "Accept-Encoding: gzip, deflate\r\n"
                + "Content-Length: " + data.length() + "\r\n";
        if (data.length() != 0) {
            expectedOutput = expectedOutput + "Content-Type: application/json\r\n";
        }

        expectedOutput = expectedOutput + "\r\n" + data;

        assertEquals(expectedOutput, new String(output.toByteArray()));
    }

    ProxyClient newTestProxyClient(OioHttp http) {
        ProxySettings settings = new ProxySettings();
        settings.url("http://127.0.0.1:8080");
        settings.ns("OPENIO");
        return new ProxyClient(http, settings);
    }

    OioUrl newContainerOioUrl() {
        return OioUrl.url(ACCOUNT_NAME, CONTAINER_NAME);
    }

    OioUrl newObjectOioUrl() {
        return OioUrl.url(ACCOUNT_NAME, CONTAINER_NAME, OBJECT_NAME);
    }

    TestSocketProvider newTestSocketProvider(int code, String msg, String data,
            Map<String, String> headers) {
        List<ByteArrayInputStream> inputs = new ArrayList<ByteArrayInputStream>();
        String input = "HTTP/1.1 " + code + " " + msg + "\r\n" + "Content-Length: " + data.length()
                + "\r\n";
        if (null != headers) {
            for (String k : headers.keySet()) {
                input += k + ": " + headers.get(k) + "\r\n";
            }
        }

        input = input + "\r\n" + data;
        inputs.add(new ByteArrayInputStream(input.getBytes()));

        return new TestSocketProvider(inputs);
    }

    @Test
    public void showReference() {
        TestSocketProvider socketProvider = newTestSocketProvider(200, "OK", "{}", null);
        OioHttp http = OioHttp.http(new OioHttpSettings(), socketProvider);
        ProxyClient proxy = newTestProxyClient(http);

        OioUrl oioURL = newContainerOioUrl();

        RequestContext reqCtx = new FixedTimeoutRequestContext();
        ReferenceInfo resp = proxy.showReference(oioURL, reqCtx);

        String expectedPath = "/v3.0/" + NAMESPACE + "/reference/show?acct=" + ACCOUNT_NAME
                + "&ref=" + CONTAINER_NAME;
        verifyRequest(socketProvider, "GET", expectedPath, "", reqCtx);

        assertNotNull(resp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void listContainerNullUrl() {

        OioHttp http = Mockito.mock(OioHttp.class);

        ProxyClient proxy = newTestProxyClient(http);
        proxy.listContainer(null, new ListOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void listContainerNullListOptions() {

        OioHttp http = Mockito.mock(OioHttp.class);
        ProxyClient proxy = newTestProxyClient(http);

        OioUrl url = newContainerOioUrl();
        proxy.listContainer(url, null);
    }

    @Test
    public void listContainer() {
        ObjectList list = new ObjectList();
        ArrayList<String> p = new ArrayList<String>();
        p.add("prefix1");
        p.add("prefix2");
        list.prefixes(p);
        ArrayList<ObjectList.ObjectView> o = new ArrayList<ObjectList.ObjectView>();
        o.add(new ObjectList.ObjectView().name("obj1"));
        o.add(new ObjectList.ObjectView().name("obj2"));
        list.objects(o);
        String data = gson().toJson(list);

        TestSocketProvider socketProvider = newTestSocketProvider(200, "OK", data, null);
        OioHttp http = OioHttp.http(new OioHttpSettings(), socketProvider);
        ProxyClient proxy = newTestProxyClient(http);

        RequestContext reqCtx = new FixedTimeoutRequestContext();
        OioUrl url = newContainerOioUrl();
        String delimiter = "/";
        String marker = "marker";
        String prefix = "prefix";
        int limit = 100;
        ListOptions listOptions = new ListOptions().delimiter(delimiter).marker(marker)
                .prefix(prefix).limit(limit);

        ObjectList resp = proxy.listContainer(url, listOptions, reqCtx);

        String expectedPath = "/v3.0/" + NAMESPACE + "/container/list?" + "acct=" + ACCOUNT_NAME
                + "&ref=" + CONTAINER_NAME + "&max=" + limit + "&prefix=" + prefix + "&marker="
                + marker + "&delimiter=" + "%2F";

        verifyRequest(socketProvider, "GET", expectedPath, "", reqCtx);

        assertNotNull(resp);
        assertEquals(resp.truncated(), false);
        List<String> prefixes = resp.prefixes();
        assertEquals(prefixes.size(), 2);
        assertEquals(prefixes.get(0), "prefix1");
        assertEquals(prefixes.get(1), "prefix2");
        List<ObjectList.ObjectView> objects = resp.objects();
        assertEquals(objects.size(), 2);
        assertEquals(objects.get(0).name(), "obj1");
        assertEquals(objects.get(1).name(), "obj2");
    }

    @Test
    public void listContainerTruncated() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(LIST_TRUNCATED_HEADER, "true");
        TestSocketProvider socketProvider = newTestSocketProvider(200, "OK", "{}", headers);
        OioHttp http = OioHttp.http(new OioHttpSettings(), socketProvider);
        ProxyClient proxy = newTestProxyClient(http);

        OioUrl url = newContainerOioUrl();
        ObjectList objectList = proxy.listContainer(url, new ListOptions());
        Assert.assertTrue(objectList.truncated());
    }
}
