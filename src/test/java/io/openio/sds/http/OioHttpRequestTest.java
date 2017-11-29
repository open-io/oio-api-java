package io.openio.sds.http;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OioHttpRequestTest {

	private OioHttpRequest testRequest(String data, String expectedMethod, String expectedURI, int nbHeaders) {
		OioHttpRequest req = null;
		try {
			req = OioHttpRequest.build(new ByteArrayInputStream(data.getBytes()));

			assertEquals(req.method(), expectedMethod);
			assertEquals(req.uri(), expectedURI);
			assertEquals(req.headers().size(), nbHeaders);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to read HTTP request");
		}
		return req;
	}

	@Test
	public void simpleNoHeadersGet() {
		OioHttpRequest req = testRequest(
				"GET /test HTTP/1.1\r\n\r\n",
				"GET",
				"/test",
				0);
	}

	@Test
	public void simpleGet() {
		OioHttpRequest req = testRequest(
				"GET /test HTTP/1.1\r\nContent-Length: 0\r\nConnection: close\r\n\r\n",
				"GET",
				"/test",
				2);

		assertEquals(req.header("Content-Length"), "0");
		assertEquals(req.header("Connection"), "close");
	}

	@Test
	public void invalidRequestLine() {
		String data = "GET /path\r\n\r\n";
		try {
			OioHttpRequest.build(new ByteArrayInputStream(data.getBytes()));
		} catch (IOException e) {
			assertTrue(e.getMessage().contains("Invalid HTTP request line"));
		}
	}
}
