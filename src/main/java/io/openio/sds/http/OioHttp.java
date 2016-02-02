package io.openio.sds.http;

import static io.openio.sds.common.JsonUtils.gson;
import static io.openio.sds.common.Strings.nullOrEmpty;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
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
    
    public RequestBuilder put(String uri) {
        Check.checkArgument(!nullOrEmpty(uri));
        return new RequestBuilder().put(uri);
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
        private InputStream data;
        private Long len;
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
        
        public RequestBuilder body(InputStream data, Long size) {
            if (nullOrEmpty(body))
                return this;
            headers.put("Content-Length", String.valueOf(size));
            headers.put("Content-Type", "application/octet-stream");
            this.data = data;
            this.len = size;
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
                OioHttpResponse resp = readResponse(sock);
                try {
                    if (null != verifier)
                        verifier.verify(resp);
                    return resp;
                } catch (SdsException e) {
                    resp.close();
                    throw e;
                }
            } catch (IOException e) {
                throw new SdsException("Http request execution error", e);
            }
        }

        public <T> T execute(Class<T> c) {
            OioHttpResponse resp = execute();
            try {
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
            if(null != data) {
                stream(sos);
            } else if (null != body) {
                sos.write(body.getBytes(Charset.defaultCharset()));
            }
            sos.flush();
        }

        private void stream(OutputStream sos) throws IOException {
            byte[] b = new byte[settings.sendBufferSize()];
            int remaining = len.intValue();
            
            while(remaining > 0) {
                int read = data.read(b, 0, Math.min(remaining, b.length));
                if(-1 == read)
                    throw new EOFException("Unexpected end of source stream");
                remaining -= read;
                sos.write(b);
            }
            data.read(b);
        }

        private byte[] requestHead() {
            StringBuilder qbuilder = new StringBuilder(
                    null == uri.getQuery() ? "" : uri.getQuery());
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
