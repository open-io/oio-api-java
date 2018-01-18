package io.openio.sds.storage.ecd;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import io.openio.sds.TestHelper;
import io.openio.sds.common.Hash;
import io.openio.sds.common.SocketProviders;
import io.openio.sds.fakeecd.FakeEcd;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ECInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.OioUrl;
import io.openio.sds.models.Position;
import io.openio.sds.proxy.ProxySettings;
import io.openio.sds.storage.rawx.RawxSettings;

public class EcdClientTest {

	private static FakeEcd fakeEcd;
	private static String fakeEcdUrl = "http://127.0.0.1:6789/";

	@BeforeClass
	public static void setup() throws Exception {
		fakeEcd = new FakeEcd(6789);
		fakeEcd.start();
	}

	@AfterClass
	public static void teardown() throws Exception {
		fakeEcd.stop();
	}

	@Test
	public void testShortData() {

		Long size = 10 * 1000 * 1024L;
		
		OioHttp http = OioHttp.http(new OioHttpSettings(),
		        SocketProviders.directSocketProvider(new OioHttpSettings()));

		EcdClient client = new EcdClient(http, new RawxSettings(),
		        new ProxySettings().ecd(fakeEcdUrl).allEcdHosts());

		byte[] data = TestHelper.bytes(size);

		OioUrl mockedUrl = Mockito.mock(OioUrl.class);
		Mockito.when(mockedUrl.cid())
		        .thenReturn(Hash.sha256()
		                .hashBytes(UUID.randomUUID().toString().getBytes())
		                .toString());
		Mockito.when(mockedUrl.object()).thenReturn("fake_object");

		List<ChunkInfo> l = new ArrayList<ChunkInfo>();
		for (int i = 0; i < 9; i++) {
			ChunkInfo ci = new ChunkInfo()
			        .pos(Position.composed(0, i))
			        .size(size)
			        .url("http://127.0.0.0.1:6010/" + Hash.sha256()
			                .hashBytes(UUID.randomUUID().toString().getBytes())
			                .toString());
			l.add(ci);
		}

		Map<Integer, List<ChunkInfo>> sorted = new HashMap<Integer, List<ChunkInfo>>();
		sorted.put(0, l);

		ObjectInfo mockedObject = Mockito.mock(ObjectInfo.class);
		Mockito.when(mockedObject.url()).thenReturn(mockedUrl);
		Mockito.when(mockedObject.size()).thenReturn(size);
		Mockito.when(mockedObject.metachunksize(Mockito.anyInt()))
		        .thenReturn(size.intValue());
		Mockito.when(mockedObject.oid()).thenReturn("B16B00B5CAFEBABE5962");
		Mockito.when(mockedObject.version())
		        .thenReturn(System.currentTimeMillis());
		Mockito.when(mockedObject.policy()).thenReturn("ECD");
		Mockito.when(mockedObject.mtype())
		        .thenReturn("application/octet-stream");
		Mockito.when(mockedObject.chunkMethod())
		        .thenReturn("ec/algo=jerasure,k=6,m=3");
		Mockito.when(mockedObject.nbchunks()).thenReturn(1);
		Mockito.when(mockedObject.sortedChunks()).thenReturn(sorted);

		client.uploadChunks(mockedObject, data);

		byte[] uploaded = fakeEcd.getLastData();

		Assert.assertEquals(data.length, uploaded.length);

		for (int i = 0; i < data.length; i++) {
			Assert.assertEquals(data[i], uploaded[i]);
		}
	}

	@Ignore
	@Test
	public void testRoundtrip() throws IOException {

		Long size = 10 * 1000 * 1024L;
		String chunkMethod = "ec/algo=liberasurecode_rs_vand,k=6,m=3";

		OioHttp http = OioHttp.http(new OioHttpSettings(),
				SocketProviders.directSocketProvider(new OioHttpSettings()));

		EcdClient client = new EcdClient(http, new RawxSettings(),
		        TestHelper.proxySettings().allEcdHosts());

		byte[] dataIn = TestHelper.bytes(size);

		OioUrl mockedUrl = Mockito.mock(OioUrl.class);
		Mockito.when(mockedUrl.cid()).thenReturn(
				Hash.sha256()
						.hashBytes(UUID.randomUUID().toString().getBytes())
						.toString());
		Mockito.when(mockedUrl.object()).thenReturn("fake_object");

		List<ChunkInfo> l = new ArrayList<ChunkInfo>();
		for (int i = 0; i < 9; i++) {
			ChunkInfo ci = new ChunkInfo()
					.pos(Position.composed(0, i))
					.size(size)
					.url("http://127.0.0.1:6010/"
							+ Hash.sha256()
									.hashBytes(
											UUID.randomUUID().toString()
													.getBytes()).toString());
			l.add(ci);
		}

		Map<Integer, List<ChunkInfo>> sorted = new HashMap<Integer, List<ChunkInfo>>();
		sorted.put(0, l);

		ObjectInfo mockedObject = Mockito.mock(ObjectInfo.class);
		Mockito.when(mockedObject.url()).thenReturn(mockedUrl);
		Mockito.when(mockedObject.size()).thenReturn(size);
		Mockito.when(mockedObject.metachunksize(Mockito.anyInt())).thenReturn(
				size.intValue());
		Mockito.when(mockedObject.oid()).thenReturn("B16B00B5CAFEBABE5962");
		Mockito.when(mockedObject.version()).thenReturn(
				System.currentTimeMillis());
		Mockito.when(mockedObject.policy()).thenReturn("EC");
		Mockito.when(mockedObject.mtype()).thenReturn(
				"application/octet-stream");
		Mockito.when(mockedObject.chunkMethod()).thenReturn(chunkMethod);
		Mockito.when(mockedObject.nbchunks()).thenReturn(1);
		Mockito.when(mockedObject.sortedChunks()).thenReturn(sorted);
		Mockito.when(mockedObject.ecinfo()).thenReturn(
				ECInfo.fromString(chunkMethod));

		client.uploadChunks(mockedObject, dataIn);

		InputStream is = client.downloadObject(mockedObject);
		DataInputStream dis = new DataInputStream(is);
		byte[] dataOut = new byte[size.intValue()];
		try {
			dis.readFully(dataOut);
			for (int i = 0; i < dataIn.length; i++) {
				Assert.assertEquals("At byte " + i, dataIn[i], dataOut[i]);
			}
		} finally {
			// TODO: delete chunks
		}
	}

}
