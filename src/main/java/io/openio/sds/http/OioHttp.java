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
import java.net.SocketException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.stream.JsonReader;

import io.openio.sds.common.Check;
import io.openio.sds.common.SocketProvider;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.exceptions.OioSystemException;
import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;

/**
 * Simple HTTP client
 * 
 * @author Christopher Dedeurwaerder
 * @author Florent Vennetier
 */
public class OioHttp {

	private static final SdsLogger logger = SdsLoggerFactory
	        .getLogger(SdsLogger.class);

	private static final String CRLF = "\r\n";

	private static final byte[] CRLF_BYTES = { '\r', '\n' };

	private OioHttpSettings settings;

	private SocketProvider socketProvider;

	private OioHttp(OioHttpSettings settings, SocketProvider socketProvider) {
		this.settings = settings;
		this.socketProvider = socketProvider;
	}

	public static OioHttp http(OioHttpSettings settings,
	        SocketProvider socketProvider) {
		Check.checkArgument(null != settings);
		Check.checkArgument(null != socketProvider);
		return new OioHttp(settings, socketProvider);
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

	public RequestBuilder delete(String uri) {
		Check.checkArgument(!nullOrEmpty(uri));
		return new RequestBuilder().delete(uri);
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
		private boolean chunked;
		private List<InetSocketAddress> alternatives = null;

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

		public RequestBuilder headers(Map<String, String> headers) {
			if (null != headers)
				this.headers.putAll(headers);
			return this;
		}

		public RequestBuilder query(String name, String value) {
			if (!nullOrEmpty(name) && !nullOrEmpty(value))
				this.query.put(name, value);
			return this;
		}

		public RequestBuilder chunked() {
			this.headers.put("transfer-encoding", "chunked");
			this.chunked = true;
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

        /**
         * @param hosts
         *            A list of hosts to contact to contact instead of the one
         *            specified in URI.
         * @return this
         */
        public RequestBuilder alternativeHosts(List<InetSocketAddress> hosts) {
            this.alternatives = hosts;
            return this;
        }

        public OioHttpResponse execute() throws OioException {
            if (this.alternatives == null || this.alternatives.isEmpty()) {
                return execute(new InetSocketAddress(uri.getHost(), uri.getPort()));
            } else {
                OioException lastExc = null;
                // TODO: implement better fallback mechanism, with randomization
                for (InetSocketAddress addr: this.alternatives) {
                    try {
                        if (lastExc != null)
                            logger.info("Retrying on " + addr.toString());
                        return execute(addr);
                    } catch (OioException oioe) {
                        // Retry only if the cause is network
                        if (oioe.getCause() instanceof IOException) {
                            logger.warn("Failed to perform request", oioe);
                            lastExc = oioe;
                        } else {
                            throw oioe;
                        }
                    }
                }
                throw new OioSystemException("Http request execution error", lastExc);
            }
        }

        private OioHttpResponse execute(InetSocketAddress addr) throws OioException {
            Socket sock = null;
            try {
                sock = socketProvider.getSocket(addr);
                if (chunked)
                    sendRequestChunked(sock);
                else
                    sendRequest(sock);
                OioHttpResponse resp = readResponse(sock);
                try {
                    if (null != verifier)
                        verifier.verify(resp);
                    return resp;
                } catch (OioException e) {
                    resp.close(true);
                    throw e;
                }
            } catch (IOException e) {
                if (null != sock)
                    try {
                        try {
                            sock.shutdownInput();
                        } catch (SocketException se) {
                            logger.debug("Socket input already shutdown");
                        }
                        sock.close();
                    } catch (IOException ioe) {
                        logger.warn("Unable to close socket, possible leak", ioe);
                    }
                throw new OioSystemException("Http request execution error", e);
            }
        }

		public <T> T execute(Class<T> c) {
			OioHttpResponse resp = execute();
			boolean success = false;
			try {
				T t = gson().fromJson(
				        new JsonReader(new InputStreamReader(resp.body(),
				                OIO_CHARSET)),
				        c);
				success = true;
				return t;
			} finally {
				resp.close(success);
			}
		}

		private OioHttpResponse readResponse(Socket sock) throws IOException {
			return OioHttpResponse.build(sock);
		}

		private void sendRequest(Socket sock) throws IOException {
			headers.put("Host", sock.getLocalAddress().toString().substring(1)
			        + ":" + sock.getLocalPort());
			headers.put("Connection", socketProvider.reusableSocket()
			        ? "keep-alive" : "close");
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

		private void sendRequestChunked(Socket sock) throws IOException {
			headers.put("Host", sock.getLocalAddress().toString().substring(1)
			        + ":" + sock.getLocalPort());
			headers.put("Connection", socketProvider.reusableSocket()
			        ? "keep-alive" : "close");
			headers.put("Accept", "*/*");
			headers.put("Accept-Encoding", "gzip, deflate");
			headers.put("User-Agent", "oio-http");

			// ensure no content-length
			headers.remove("Content-Length");
			OutputStream sos = sock.getOutputStream();
			byte[] requestHead = requestHead();
			
			sos.write(requestHead);
			streamChunked(sos);
			sos.flush();
		}

		private void streamChunked(OutputStream sos) throws IOException {
			byte[] b = new byte[settings.sendBufferSize()];
			int remaining = len.intValue();
			while (remaining > 0) {
				int read = data.read(b, 0, Math.min(remaining, b.length));
				if (-1 == read)
					throw new EOFException("Unexpected end of source stream");
				remaining -= read;
				if (read > 0) {
					sos.write((Integer.toHexString(read) + CRLF)
					        .getBytes(OIO_CHARSET));
					sos.write(b, 0, read);
					sos.write(CRLF_BYTES);
				}
			}
			sos.write(("0" + CRLF + CRLF)
			        .getBytes(OIO_CHARSET));
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
			        .append("HTTP/1.1").append(CRLF);
			for (Entry<String, String> h : headers.entrySet()) {
				req.append(h.getKey()).append(": ").append(h.getValue())
				        .append(CRLF);
			}
			byte[] res = req.append(CRLF)
			        .toString()
			        .getBytes(OIO_CHARSET);
			return res;
		}
	}
}
