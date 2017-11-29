package io.openio.sds.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static io.openio.sds.common.OioConstants.OIO_CHARSET;
import static io.openio.sds.common.Strings.nullOrEmpty;
import static java.lang.String.format;

public class OioHttpRequest {

	private static final int R = 1;
	private static final int RN = 2;
	private static final int RNR = 3;
	private static final int RNRN = 3;

	private static byte BS_R = '\r';
	private static byte BS_N = '\n';

	private RequestHead head;
	private InputStream is;

	public OioHttpRequest(InputStream is) {
		this.is = is;
	}

	public static OioHttpRequest build(InputStream is) throws IOException {
		OioHttpRequest r = new OioHttpRequest(is);
		r.parseHead();
		return r;
	}

	private void parseHead() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int state = 0;
		while (state < RNRN) {
			state = next(bos, state);
		}
		read();
		head = RequestHead.parse(new String(bos.toByteArray(), OIO_CHARSET));
	}

	private int next(ByteArrayOutputStream bos, int state) throws IOException {
		int b = read();
		bos.write(b);
		switch (state) {
			case R:
				return b == BS_N ? RN : b == BS_R ? R : 0;
			case RN:
				return b == BS_R ? RNR : 0;
			case RNR:
				return b == BS_N ? RNRN : b == BS_R ? R : 0;
			default:
				return b == BS_R ? R : 0;
		}
	}

	private int read() throws IOException {
		int b = is.read();
		if (-1 == b)
			throw new IOException("Unexpected end of stream");
		return b;
	}

	public String method() {
		return head.method();
	}

	public String uri() {
		return head.uri();
	}

	public Map<String, String> headers() {
		return head.headers();
	}

	public String header(String key) {
		return head.headers().get(key.toLowerCase());
	}

	public static class RequestHead {

		private RequestLine requestLine;
		private BufferedReader reader;
		private HashMap<String, String> headers = new HashMap<String, String>();


		private RequestHead(BufferedReader reader) {
			this.reader = reader;
		}

		static RequestHead parse(String head) throws IOException {
			RequestHead h = new RequestHead(new BufferedReader(new StringReader(head)));
			h.parseRequestLine();
			h.parseHeaders();
			return h;
		}

		private void parseRequestLine() throws IOException {
			requestLine = RequestLine.parse(reader.readLine());
		}

		private void parseHeaders() throws IOException {
			String line;
			while (null != (line = reader.readLine())) {
				if (nullOrEmpty(line))
					continue;
				String[] tok = line.trim().split(":", 2);
				if (2 != tok.length)
					continue;
				headers.put(tok[0].trim().toLowerCase(), tok[1].trim());
			}
		}

		public Map<String, String> headers() {
			return headers;
		}

		public String method() {
			return requestLine.method;
		}

		public String uri() {
			return requestLine.uri;
		}
	}

	public static class RequestLine {
		private String method;
		private String uri;
		private String proto;

		RequestLine(String method, String uri, String proto) {
			this.method = method;
			this.uri = uri;
			this.proto = proto;
		}

		static RequestLine parse(String line) throws IOException {
			String[] tok = line.trim().split(" ", 3);
			if (3 != tok.length) {
				throw new IOException(format("Invalid HTTP request line: %s", line));
			}
			return new RequestLine(tok[0], tok[1], tok[2]);
		}
	}
}
