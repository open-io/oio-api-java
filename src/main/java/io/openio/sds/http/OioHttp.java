package io.openio.sds.http;

import static io.openio.sds.common.JsonUtils.gson;
import static io.openio.sds.common.OioConstants.CONTENT_LENGTH_HEADER;
import static io.openio.sds.common.OioConstants.CONTENT_TYPE_HEADER;
import static io.openio.sds.common.OioConstants.DELETE_METHOD;
import static io.openio.sds.common.OioConstants.GET_METHOD;
import static io.openio.sds.common.OioConstants.OIO_CHARSET;
import static io.openio.sds.common.OioConstants.POST_METHOD;
import static io.openio.sds.common.OioConstants.PUT_METHOD;
import static io.openio.sds.common.Strings.nullOrEmpty;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.stream.JsonReader;

import io.openio.sds.common.Check;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.exceptions.OioSystemException;
import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;

/**
 * Simple HTTP client
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class OioHttp {

    private static final SdsLogger logger = SdsLoggerFactory
            .getLogger(SdsLogger.class);

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
            return req(POST_METHOD, url);
        }

        public RequestBuilder put(String url) {
            return req(PUT_METHOD, url);
        }

        public RequestBuilder get(String url) {
            return req(GET_METHOD, url);
        }

        public RequestBuilder delete(String url) {
            return req(DELETE_METHOD, url);
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
            headers.put(CONTENT_LENGTH_HEADER, String.valueOf(body.length()));
            headers.put(CONTENT_TYPE_HEADER, "application/json");
            this.body = body;
            return this;
        }

        public RequestBuilder body(InputStream data, Long size) {
            if (null == data)
                return this;
            headers.put(CONTENT_LENGTH_HEADER, String.valueOf(size));
            headers.put(CONTENT_TYPE_HEADER, "application/octet-stream");
            this.data = data;
            this.len = size;
            return this;
        }

        public RequestBuilder verifier(OioHttpResponseVerifier verifier) {
            this.verifier = verifier;
            return this;
        }

        public OioHttpResponse execute() throws OioException {
            Socket sock = null;
            try {
                sock = new Socket();
                sock.setSendBufferSize(settings.sendBufferSize());
                sock.setReuseAddress(true);
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
                } catch (OioException e) {
                    resp.close();
                    throw e;
                }
            } catch (IOException e) {
                if (null != sock && !sock.isClosed())
                    try {
                        sock.close();
                    } catch (IOException ioe) {
                        logger.warn("Unable to close socket, possible leak", ioe);
                    }
                throw new OioSystemException("Http request execution error", e);
            }
        }

        public <T> T execute(Class<T> c) {
            OioHttpResponse resp = execute();
            try {
                return gson().fromJson(
                        new JsonReader(new InputStreamReader(resp.body(),
                                OIO_CHARSET)),
                        c);
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
            headers.put("Connection", "close");
            headers.put("Accept", "*/*");
            headers.put("Accept-Encoding", "gzip, deflate");
            headers.put("User-Agent", "oio-http");

            if (!headers.containsKey("Content-Length"))
                headers.put(CONTENT_LENGTH_HEADER, "0");
            OutputStream sos = sock.getOutputStream();
            sos.write(requestHead());
            if (null != data) {
                stream(sos);
            } else if (null != body) {
                sos.write(body.getBytes(OIO_CHARSET));
            }
            sos.flush();
        }

        private void stream(OutputStream sos) throws IOException {
            byte[] b = new byte[settings.sendBufferSize()];
            int remaining = len.intValue();

            while (remaining > 0) {
                int read = data.read(b, 0, Math.min(remaining, b.length));
                if (-1 == read)
                    throw new EOFException("Unexpected end of source stream");
                remaining -= read;
                sos.write(b, 0, read);
            }
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
                    .getBytes(OIO_CHARSET);
            return res;
        }
    }
}
