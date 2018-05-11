package io.openio.sds.storage.rawx;

import io.openio.sds.RequestContext;
import io.openio.sds.TestHelper;
import io.openio.sds.TestSocketProvider;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttpRequest;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.OioUrl;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static io.openio.sds.common.IdGen.requestId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RawxClientTest {

	String NAMESPACE = "OPENIO";
	String ACCOUNT_NAME = "testaccount";
	String CONTAINER_NAME = "testcontainer";
	String OBJECT_NAME = "testobject";

	OioUrl newObjectOioUrl() {
		return OioUrl.url(ACCOUNT_NAME, CONTAINER_NAME, OBJECT_NAME);
	}


	void verifyGetRequests(TestSocketProvider socketProvider, ObjectInfo info,
	        RequestContext reqCtx) {
		List<ByteArrayOutputStream> outputs = socketProvider.outputs();
		assertEquals(outputs.size(), 1);
		ByteArrayOutputStream output = outputs.get(0);

		try {
			OioHttpRequest req = OioHttpRequest.build(new ByteArrayInputStream(output.toByteArray()));
			assertEquals(req.method(), "GET");
			assertEquals(req.header("X-oio-req-id"), reqCtx.requestId());
			// TODO verify matching chunk
		} catch (IOException e) {
			fail("Failed to read HTTP request");
		}
	}


	@Test
	public void simple() {
		List<ByteArrayInputStream> inputs = new ArrayList<ByteArrayInputStream>();
		inputs.add(new ByteArrayInputStream("HTTP/1.0 200 OK\r\nContent-Length: 0\r\n\r\n".getBytes()));
		inputs.add(new ByteArrayInputStream("HTTP/1.0 200 OK\r\nContent-Length: 0\r\n\r\n".getBytes()));
		inputs.add(new ByteArrayInputStream("HTTP/1.0 200 OK\r\nContent-Length: 0\r\n\r\n".getBytes()));

		TestSocketProvider socketProvider = new TestSocketProvider(inputs);

		OioHttp http = OioHttp.http(new OioHttpSettings(), socketProvider);

		RawxSettings rawxSettings = new RawxSettings();
		RawxClient client = new RawxClient(http, rawxSettings);

		OioUrl url = newObjectOioUrl();

		String testData = "test";
		byte[] dataBytes = testData.getBytes();

		RequestContext reqCtx = new RequestContext();
		ObjectInfo objectInfo = TestHelper.newTestObjectInfo(url, dataBytes.length);
		client.uploadChunks(objectInfo, dataBytes, reqCtx);

		// TODO verify PUT requests
	}

	@Test
	public void shortRead() {
		final List<ByteArrayInputStream> inputs = new ArrayList<ByteArrayInputStream>();
		inputs.add(new ByteArrayInputStream("HTTP/1.0 200 OK\r\nContent-Length: 0\r\n\r\n".getBytes()));
		inputs.add(new ByteArrayInputStream("HTTP/1.0 200 OK\r\nContent-Length: 0\r\n\r\n".getBytes()));
		inputs.add(new ByteArrayInputStream("HTTP/1.0 200 OK\r\nContent-Length: 0\r\n\r\n".getBytes()));

		TestSocketProvider socketProvider = new TestSocketProvider(inputs);


		OioHttp http = OioHttp.http(new OioHttpSettings(), socketProvider);

		RawxSettings rawxSettings = new RawxSettings();
		RawxClient client = new RawxClient(http, rawxSettings);

		OioUrl url = newObjectOioUrl();


		String testData = "test";
		byte[] dataBytes = testData.getBytes();

		ObjectInfo objectInfo = TestHelper.newTestObjectInfo(url, dataBytes.length + 1);

		try {
			client.uploadChunks(objectInfo, dataBytes);
			fail("Expected OioException");
		} catch (OioException e) {
			assertTrue(e.getMessage().contains("Stream read error"));
			assertTrue(e.getCause() instanceof EOFException);
		}
	}

	@Test
	public void downloadEmpty() {
		final List<ByteArrayInputStream> inputs = new ArrayList<ByteArrayInputStream>();
		inputs.add(new ByteArrayInputStream("HTTP/1.0 200 OK\r\nContent-Length: 0\r\n\r\n".getBytes()));

		TestSocketProvider socketProvider = new TestSocketProvider(inputs);


		OioHttp http = OioHttp.http(new OioHttpSettings(), socketProvider);

		RawxSettings rawxSettings = new RawxSettings();
		RawxClient client = new RawxClient(http, rawxSettings);

		OioUrl url = newObjectOioUrl();

		RequestContext reqCtx = new RequestContext();
		ObjectInfo objectInfo = TestHelper.newTestObjectInfo(url, 0);

		InputStream stream = client.downloadObject(objectInfo, reqCtx);

		try {
			assertEquals(stream.read(), -1);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected IOException");
		}

		verifyGetRequests(socketProvider, objectInfo, reqCtx);
	}

	@Test
	public void download() {
		final List<ByteArrayInputStream> inputs = new ArrayList<ByteArrayInputStream>();
		inputs.add(new ByteArrayInputStream("HTTP/1.0 200 OK\r\nContent-Length: 4\r\n\r\ntest".getBytes()));

		TestSocketProvider socketProvider = new TestSocketProvider(inputs);

		OioHttp http = OioHttp.http(new OioHttpSettings(), socketProvider);

		RawxSettings rawxSettings = new RawxSettings();
		RawxClient client = new RawxClient(http, rawxSettings);

		OioUrl url = newObjectOioUrl();

		RequestContext reqCtx = new RequestContext();
		ObjectInfo objectInfo = TestHelper.newTestObjectInfo(url, 4);

		InputStream stream = client.downloadObject(objectInfo, reqCtx);

		try {
			assertEquals(new String(TestHelper.toByteArray(stream)),"test");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected IOException");
		}

		verifyGetRequests(socketProvider, objectInfo, reqCtx);
	}
}
