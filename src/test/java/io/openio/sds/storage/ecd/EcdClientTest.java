package io.openio.sds.storage.ecd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.openio.sds.TestHelper;
import io.openio.sds.common.Hash;
import io.openio.sds.common.SocketProviders;
import io.openio.sds.fakeecd.FakeEcd;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.OioUrl;
import io.openio.sds.models.Position;
import io.openio.sds.storage.rawx.RawxSettings;

public class EcdClientTest {

	private static FakeEcd ecd;
	private static String ecdUrl = "http://127.0.0.1:6789/";

	@BeforeClass
	public static void setup() throws Exception {
		ecd = new FakeEcd(6789);
		ecd.start();
	}

	@AfterClass
	public static void teardown() throws Exception {
		ecd.stop();
	}

	@Test
	public void testShortData() {

		Long size = 10 * 1000 * 1024L;
		
		OioHttp http = OioHttp.http(new OioHttpSettings(),
		        SocketProviders.directSocketProvider(new OioHttpSettings()));

		EcdClient client = new EcdClient(http, new RawxSettings(), ecdUrl);

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
			        .pos(Position.composed(0, i, false))
			        .size(10 * 1024 * 1024L)
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
		Mockito.when(mockedObject.oid()).thenReturn("fakeoid");
		Mockito.when(mockedObject.version())
		        .thenReturn(System.currentTimeMillis());
		Mockito.when(mockedObject.policy()).thenReturn("ECD");
		Mockito.when(mockedObject.mtype())
		        .thenReturn("application/octet-stream");
		Mockito.when(mockedObject.chunkMethod())
		        .thenReturn("ecd/algo=jerasure,k=6,m=3");
		Mockito.when(mockedObject.nbchunks()).thenReturn(1);
		Mockito.when(mockedObject.sortedChunks()).thenReturn(sorted);

		client.uploadChunks(mockedObject, data);

		byte[] uploaded = ecd.getLastData();

		Assert.assertEquals(data.length, uploaded.length);

		for (int i = 0; i < data.length; i++) {
			Assert.assertEquals(data[i], uploaded[i]);
		}
	}

}
