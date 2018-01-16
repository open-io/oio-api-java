package io.openio.sds.http;

import io.openio.sds.RequestContext;
import io.openio.sds.TestHelper;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OioHttpResponseTest {


    private OioHttpResponse testResponse(String data, int expectedCode, String expectedMsg,
            int nbHeaders) {
        OioHttpResponse resp = null;
        Socket sock = mock(Socket.class);
        try {
            when(sock.getInputStream()).thenReturn(new ByteArrayInputStream(data.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            RequestContext reqCtx = new RequestContext();
            resp = OioHttpResponse.build(sock, reqCtx);

            assertEquals(resp.code(), expectedCode);
            assertEquals(resp.msg(), expectedMsg);
            assertEquals(resp.headers().size(), nbHeaders);
            assertEquals(reqCtx, resp.requestContext());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to read HTTP response");
        }
        return resp;
    }

	@Test
	public void simpleNoHeaders200() {
		OioHttpResponse resp = testResponse(
				"HTTP/1.1 200 OK\r\n\r\n",
				200,
				"OK",
				0);
	}


	@Test
	public void simple200() {
		OioHttpResponse resp = testResponse(
				"HTTP/1.1 200 OK\r\nContent-Length: 0\r\nConnection: close\r\n\r\n",
				200,
				"OK",
				2);
		assertEquals(resp.header("Content-Length"), "0");
		assertEquals(resp.header("Connection"), "close");
	}

	@Test
	public void body200() {
		OioHttpResponse resp = testResponse(
				"HTTP/1.1 200 OK\r\nContent-Length: 4\r\n\r\ntest",
				200,
				"OK",
				1);

		assertEquals(resp.length().longValue(), 4);
		InputStream is = resp.body();
		try {
			byte[] body = TestHelper.toByteArray(is);
			assertEquals(new String(body), "test");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to read HTTP response body");
		}
	}

	@Test
	public void body500() {
		OioHttpResponse resp = testResponse(
				"HTTP/1.1 500 Internal Server Error\r\nContent-Length: 5\r\n\r\nerror",
				500,
				"Internal Server Error",
				1);

		assertEquals(resp.length().longValue(), 5);
		InputStream is = resp.body();
		try {
			byte[] body = TestHelper.toByteArray(is);
			assertEquals(new String(body), "error");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to read HTTP response body");
		}

		resp.close();
	}

	@Test
	public void invalidStatusLine() {
		String data = "HTTP/1.1 200OK\r\n\r\n";
		Socket sock = mock(Socket.class);
		try {
			when(sock.getInputStream()).thenReturn(new ByteArrayInputStream(data.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			OioHttpResponse.build(sock, null);
			fail("Expected IOException");
		} catch (IOException e) {
			assertTrue(e.getMessage().contains("Invalid HTTP status line"));
		}
	}
}
