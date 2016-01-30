package io.openio.sds.http;

import static io.openio.sds.common.Strings.nullOrEmpty;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map.Entry;

import io.openio.sds.common.Check;
import io.openio.sds.exceptions.SdsException;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class OioHttp {

    private static final String EOL = "\r\n";

    private HttpSettings settings;

    private OioHttp(HttpSettings settings) {
        this.settings = settings;
    }

    public static OioHttp http(HttpSettings settings) {
        Check.checkArgument(null != settings);
        return new OioHttp(settings);
    }

    public RequestBuilder post(String uri) {
        Check.checkArgument(!nullOrEmpty(uri));
        return new RequestBuilder().post(uri);
    }

    public RequestBuilder get(String uri) {
        Check.checkArgument(!nullOrEmpty(uri));
        return new RequestBuilder().get(uri);
    }

    public class RequestBuilder {

        private String method;
        private HashMap<String, String> headers = new HashMap<String, String>();
        private String body;
        private URI uri;

        public RequestBuilder post(String url) {
            return req("POST", url);
        }

        public RequestBuilder put(String url) {
            return req("PUT", url);
        }

        public RequestBuilder get(String url) {
            return req("GET", url);
        }

        public RequestBuilder delete(String url) {
            return req("DELETE", url);
        }

        public RequestBuilder req(String method, String url) {
            this.method = method;
            this.uri = URI.create(url);
            return this;
        }

        public RequestBuilder header(String name, String value) {
            if (!nullOrEmpty(name) || !nullOrEmpty(value))
                headers.put(name, value);
            return this;
        }

        public RequestBuilder body(String body) {
            headers.put("Content-Type", "application/json");
            this.body = body;
            return this;
        }

        public OioHttpResponse execute() throws SdsException {
            try {
                Socket sock = new Socket();
                sock.setSendBufferSize(settings.sendBufferSize());
                sock.setReceiveBufferSize(settings.receiveBufferSize());
                System.out.println(uri.getHost());
                System.out.println(uri.getPort());
                sock.connect(
                        new InetSocketAddress(uri.getHost(), uri.getPort()),
                        settings.connectTimeout());
                sendRequest(sock);
                return readResponse(sock);
            } catch (Exception e) {
                throw new SdsException("Http request execution error", e);
            }
        }

        public <T> T execute(Class<T> c) {

            return null;
        }

        private OioHttpResponse readResponse(Socket sock) throws IOException {
            return OioHttpResponse.build(sock);
        }

        private void sendRequest(Socket sock) throws IOException {
            OutputStream sos = sock.getOutputStream();
            sos.write(requestHead());
            if (null != body)
                sos.write(body.getBytes(Charset.defaultCharset()));
            sos.flush();
        }

        private byte[] requestHead() {
            StringBuilder req = new StringBuilder(method)
                    .append(" ")
                    .append(uri.getPath())
                    .append(" ")
                    .append("HTTP/1.1").append(EOL);
            for (Entry<String, String> h : headers.entrySet()) {
                req.append(h.getKey()).append(": ").append(h.getValue())
                        .append(EOL);
            }
            byte[] res = req.append(EOL)
                    .toString()
                    .getBytes(Charset.defaultCharset());
            System.out.println("Request head: ");
            System.out.println(new String(res));
            return res;
        }
    }
}
