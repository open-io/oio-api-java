package io.openio.sds.http;

import static io.openio.sds.common.JsonUtils.gson;
import static io.openio.sds.common.Strings.nullOrEmpty;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.stream.JsonReader;

import io.openio.sds.common.Check;
import io.openio.sds.exceptions.SdsException;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class OioHttp {

    private static final String EOL = "\r\n";

    private OioHttpSettings settings;

    private OioHttp(OioHttpSettings settings) {
        this.settings = settings;
    }

    public static OioHttp http(OioHttpSettings settings) {
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
        private HashMap<String, String> query = new HashMap<String, String>();
        private String body;
        private URI uri;
        private OioHttpResponseVerifier verifier = null;

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
            if (!nullOrEmpty(name) && !nullOrEmpty(value))
                headers.put(name, value);
            return this;
        }

        public RequestBuilder query(String name, String value) {
            if (!nullOrEmpty(name) && !nullOrEmpty(value))
                this.query.put(name, value);
            return this;
        }

        public RequestBuilder body(String body) {
            if (nullOrEmpty(body))
                return this;
            headers.put("Content-Length", String.valueOf(body.length()));
            headers.put("Content-Type", "application/json");
            this.body = body;
            return this;
        }

        public RequestBuilder verifier(OioHttpResponseVerifier verifier) {
            this.verifier = verifier;
            return this;
        }

        public OioHttpResponse execute() throws SdsException {
            try {
                Socket sock = new Socket();
                sock.setSendBufferSize(settings.sendBufferSize());
                sock.setReceiveBufferSize(settings.receiveBufferSize());
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
            OioHttpResponse resp = execute();
            try {
                if (null != verifier)
                    verifier.verify(resp);
                return gson().fromJson(
                        new JsonReader(new InputStreamReader(resp.body())), c);
            } finally {
                resp.close();
            }
        }

        private OioHttpResponse readResponse(Socket sock) throws IOException {
            return OioHttpResponse.build(sock);
        }

        private void sendRequest(Socket sock) throws IOException {
            headers.put("Host", sock.getLocalAddress().toString().substring(1)
                    + ":" + sock.getLocalPort());
            headers.put("Connection", "keep-alive");
            headers.put("Accept", "*/*");
            headers.put("Accept-Encoding", "gzip, deflate");
            headers.put("User-Agent", "oio-http");

            if (!headers.containsKey("Content-Length"))
                headers.put("Content-Length", "0");
            OutputStream sos = sock.getOutputStream();
            sos.write(requestHead());
            if (null != body)
                sos.write(body.getBytes(Charset.defaultCharset()));
            sos.flush();
        }

        private byte[] requestHead() {
            StringBuilder qbuilder = new StringBuilder(uri.getQuery());
            boolean removeTrailindAnd = 0 == qbuilder.length();
            for (Entry<String, String> h : query.entrySet()) {
                qbuilder.append("&")
                        .append(h.getKey())
                        .append("=")
                        .append(h.getValue());
            }
            StringBuilder req = new StringBuilder(method)
                    .append(" ")
                    .append(uri.getPath())
                    .append(qbuilder.length() > 0 ? "?" : "")
                    .append(qbuilder.length() > 0 ? (removeTrailindAnd
                            ? qbuilder.substring(1) : qbuilder.toString()) : "")
                    .append(" ")
                    .append("HTTP/1.1").append(EOL);
            for (Entry<String, String> h : headers.entrySet()) {
                req.append(h.getKey()).append(": ").append(h.getValue())
                        .append(EOL);
            }
            byte[] res = req.append(EOL)
                    .toString()
                    .getBytes(Charset.defaultCharset());
            System.out.println(new String(res));
            return res;
        }
    }
}
