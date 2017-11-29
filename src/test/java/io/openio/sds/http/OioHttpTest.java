package io.openio.sds.http;

import io.openio.sds.TestHelper;
import io.openio.sds.TestSocketProvider;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class OioHttpTest {

	@Test
	public void simplePost() {
		List<ByteArrayInputStream> inputs = new ArrayList<ByteArrayInputStream>();
		inputs.add(new ByteArrayInputStream("HTTP/1.0 200 OK\r\nContent-Length: 0\r\n\r\n".getBytes()));

		TestSocketProvider socketProvider = new TestSocketProvider(inputs);

		OioHttp http = OioHttp.http(new OioHttpSettings(), socketProvider);

		OioHttp.RequestBuilder req = http.post("http://127.0.0.1:8080/testPath");
		req.header("TestHeaderKey", "testHeaderValue");
		req.header("TestHeaderKey2", "testHeaderValue2");

		req.query("testKey", "testValue");
		req.query("@", "=");

		req.body("{}");

		OioHttpResponse resp = req.execute();

		List<ByteArrayOutputStream> outputs = socketProvider.outputs();
		assertEquals(outputs.size(), 1);
		ByteArrayOutputStream output = outputs.get(0);

		String expectedOutput =
				"POST /testPath?%40=%3D&testKey=testValue HTTP/1.1\r\n" +
						"TestHeaderKey2: testHeaderValue2\r\n" +
						"TestHeaderKey: testHeaderValue\r\n" +
						"Accept: */*\r\n" +
						"Connection: close\r\n" +
						"User-Agent: oio-http\r\n" +
						"Host: 127.0.0.1:8080\r\n" +
						"Accept-Encoding: gzip, deflate\r\n" +
						"Content-Length: 2\r\n" +
						"Content-Type: application/json\r\n" +
						"\r\n" +
						"{}";

		assertEquals(new String(output.toByteArray()), expectedOutput);
		assertEquals(resp.code(), 200);
		assertEquals(resp.msg(), "OK");
		assertEquals(resp.headers().size(), 1);
		assertEquals(resp.header("Content-Length"), "0");
		assertEquals(resp.length().longValue(), 0);
	}

	@Test
	public void chunked() {
		List<ByteArrayInputStream> inputs = new ArrayList<ByteArrayInputStream>();
		inputs.add(new ByteArrayInputStream("HTTP/1.0 200 OK\r\nContent-Length: 0\r\n\r\n".getBytes()));

		TestSocketProvider socketProvider = new TestSocketProvider(inputs);

		OioHttpSettings httpSettings = new OioHttpSettings();
		// change the send buffer size to test the chunked transfer
		httpSettings.sendBufferSize(3);
		OioHttp http = OioHttp.http(httpSettings, socketProvider);

		OioHttp.RequestBuilder req = http.post("http://127.0.0.1:8080/testPath");

		req.chunked();

		byte[] data = "test".getBytes();

		ByteArrayInputStream input = new ByteArrayInputStream(data);
		req.body(input, (long) data.length);
		req.execute();

		List<ByteArrayOutputStream> outputs = socketProvider.outputs();
		assertEquals(outputs.size(), 1);
		ByteArrayOutputStream output = outputs.get(0);

		String expectedOutput =
				"POST /testPath HTTP/1.1\r\n" +
						"Transfer-Encoding: chunked\r\n" +
						"Accept: */*\r\n" +
						"Connection: close\r\n" +
						"User-Agent: oio-http\r\n" +
						"Host: 127.0.0.1:8080\r\n" +
						"Accept-Encoding: gzip, deflate\r\n" +
						"Content-Type: application/octet-stream\r\n" +
						"\r\n" +
						"3\r\n" +
						"tes\r\n" +
						"1\r\n" +
						"t\r\n" +
						"0\r\n\r\n";

		assertEquals(new String(output.toByteArray()), expectedOutput);
	}

	@Test
	public void stream() {
		List<ByteArrayInputStream> inputs = new ArrayList<ByteArrayInputStream>();
		inputs.add(new ByteArrayInputStream("HTTP/1.0 200 OK\r\nContent-Length: 0\r\n\r\n".getBytes()));

		TestSocketProvider socketProvider = new TestSocketProvider(inputs);

		OioHttpSettings httpSettings = new OioHttpSettings();
		OioHttp http = OioHttp.http(httpSettings, socketProvider);

		OioHttp.RequestBuilder req = http.post("http://127.0.0.1:8080/testPath");

		byte[] data = "test".getBytes();

		ByteArrayInputStream input = new ByteArrayInputStream(data);
		req.body(input, (long) data.length);
		req.execute();

		List<ByteArrayOutputStream> outputs = socketProvider.outputs();
		assertEquals(outputs.size(), 1);
		ByteArrayOutputStream output = outputs.get(0);

		String expectedOutput =
				"POST /testPath HTTP/1.1\r\n" +
						"Accept: */*\r\n" +
						"Connection: close\r\n" +
						"User-Agent: oio-http\r\n" +
						"Host: 127.0.0.1:8080\r\n" +
						"Accept-Encoding: gzip, deflate\r\n" +
						"Content-Length: 4\r\n" +
						"Content-Type: application/octet-stream\r\n" +
						"\r\n" +
						"test";

		assertEquals(new String(output.toByteArray()), expectedOutput);
	}

	class Dummy {
		String a;
		int b;
		boolean c;

		public Dummy() {
		}
	}

	@Test
	public void json() {
		List<ByteArrayInputStream> inputs = new ArrayList<ByteArrayInputStream>();
		String input = "HTTP/1.0 200 OK\r\n" +
				"Content-Length: 31\r\n" +
				"\r\n" +
				"{\"a\":\"test\"," +
				" \"b\":11," +
				" \"c\":false}";

		inputs.add(new ByteArrayInputStream(input.getBytes()));

		TestSocketProvider socketProvider = new TestSocketProvider(inputs);

		OioHttpSettings httpSettings = new OioHttpSettings();
		OioHttp http = OioHttp.http(httpSettings, socketProvider);


		OioHttp.RequestBuilder req = http.post("http://127.0.0.1:8080/testPath");
		Dummy dummy = req.execute(Dummy.class);

		assertNotNull(dummy);
		assertEquals(dummy.a, "test");
		assertEquals(dummy.b, 11);
		assertEquals(dummy.c, false);
	}

	@Test
	public void responseBody() {
		List<ByteArrayInputStream> inputs = new ArrayList<ByteArrayInputStream>();
		String input = "HTTP/1.0 200 OK\r\n" +
				"Content-Length: 4\r\n" +
				"\r\n" +
				"test" +
				// add garbage data at the end
				"garbage";

		inputs.add(new ByteArrayInputStream(input.getBytes()));

		TestSocketProvider socketProvider = new TestSocketProvider(inputs);

		OioHttpSettings httpSettings = new OioHttpSettings();
		OioHttp http = OioHttp.http(httpSettings, socketProvider);

		OioHttp.RequestBuilder req = http.post("http://127.0.0.1:8080/testPath");
		OioHttpResponse resp = req.execute();

		String expectedOutput = "test";

		try {
			byte[] body = TestHelper.toByteArray(resp.body());
			assertEquals(new String(body), expectedOutput);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to read HTTP response");
		}
	}

	@Test
	public void emptyBody() {
		List<ByteArrayInputStream> inputs = new ArrayList<ByteArrayInputStream>();
		String input = "HTTP/1.0 200 OK\r\n" +
				"Content-Length: 0\r\n" +
				"\r\n";

		inputs.add(new ByteArrayInputStream(input.getBytes()));

		TestSocketProvider socketProvider = new TestSocketProvider(inputs);

		OioHttpSettings httpSettings = new OioHttpSettings();
		OioHttp http = OioHttp.http(httpSettings, socketProvider);

		OioHttp.RequestBuilder req = http.post("http://127.0.0.1:8080/testPath");
		OioHttpResponse resp = req.execute();

		try {
			assertEquals(resp.body().read(), -1);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to read HTTP response");
		}
	}
}
