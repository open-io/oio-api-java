package io.openio.sds.storage.ecd;

import static io.openio.sds.common.IdGen.requestId;
import static io.openio.sds.common.OioConstants.CHUNK_META_CHUNK_POS;
import static io.openio.sds.common.OioConstants.CHUNK_META_CONTAINER_ID;
import static io.openio.sds.common.OioConstants.CHUNK_META_CONTENT_CHUNKSNB;
import static io.openio.sds.common.OioConstants.CHUNK_META_CONTENT_CHUNK_METHOD;
import static io.openio.sds.common.OioConstants.CHUNK_META_CONTENT_ID;
import static io.openio.sds.common.OioConstants.CHUNK_META_CONTENT_MIME_TYPE;
import static io.openio.sds.common.OioConstants.CHUNK_META_CONTENT_PATH;
import static io.openio.sds.common.OioConstants.CHUNK_META_CONTENT_POLICY;
import static io.openio.sds.common.OioConstants.CHUNK_META_CONTENT_SIZE;
import static io.openio.sds.common.OioConstants.CHUNK_META_CONTENT_VERSION;
import static io.openio.sds.common.OioConstants.OIO_REQUEST_ID_HEADER;
import static io.openio.sds.http.Verifiers.RAWX_VERIFIER;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import io.openio.sds.common.Hex;
import io.openio.sds.common.OioConstants;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttp.RequestBuilder;
import io.openio.sds.http.OioHttpResponse;
import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.storage.StorageClient;
import io.openio.sds.storage.rawx.RawxClient;
import io.openio.sds.storage.rawx.RawxSettings;
import io.openio.sds.storage.rawx.StreamWrapper;

public class EcdClient implements StorageClient {

	private static final SdsLogger logger = SdsLoggerFactory
	        .getLogger(RawxClient.class);

	final OioHttp http;
	private final RawxSettings settings;
	private final String ecdUrl;

	public EcdClient(OioHttp http, RawxSettings settings, String ecdUrl) {
		this.http = http;
		this.settings = settings;
		this.ecdUrl = ecdUrl;
	}

	@Override
	public ObjectInfo uploadChunks(ObjectInfo oinf, InputStream data) {
		return uploadChunks(oinf, data, requestId());

	}

	@Override
	public ObjectInfo uploadChunks(ObjectInfo oinf, InputStream data,
	        String reqId) {
		StreamWrapper wrapper = new StreamWrapper(data);
		long remaining = oinf.size();
		for (int pos = 0; pos < oinf.nbchunks(); pos++) {
			long csize = Math.min(remaining, oinf.metachunksize(pos));
			uploadPosition(oinf, pos, csize, wrapper, reqId);
			remaining -= csize;
		}
		return oinf.hash(Hex.toHex(wrapper.md5()));
	}

	@Override
	public ObjectInfo uploadChunks(ObjectInfo oinf, File data) {
		return uploadChunks(oinf, data, requestId());
	}

	@Override
	public ObjectInfo uploadChunks(ObjectInfo oinf, File data, String reqId) {
		try {
			FileInputStream fin = new FileInputStream(data);
			try {
				return uploadChunks(oinf, fin, reqId);
			} finally {
				try {
					fin.close();
				} catch (IOException e) {
					logger.warn("Fail to close Inputstream, possible leak", e);
				}
			}
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File not found", e);
		}
	}

	@Override
	public ObjectInfo uploadChunks(ObjectInfo oinf, byte[] data) {
		return uploadChunks(oinf, data, requestId());

	}

	@Override
	public ObjectInfo uploadChunks(ObjectInfo oinf, byte[] data, String reqId) {
		return uploadChunks(oinf, new ByteArrayInputStream(data), reqId);

	}

	@Override
	public InputStream downloadObject(ObjectInfo oinf) {
		return downloadObject(oinf, requestId());
	}

	@Override
	public InputStream downloadObject(ObjectInfo oinf, String reqId) {
		return new EcdInputStream(ecdUrl, oinf, http, reqId);
	}

	/* --- INTERNALS --- */

	private ObjectInfo uploadPosition(final ObjectInfo oinf,
	        final int pos, final Long size, InputStream data,
	        final String reqId) {

		RequestBuilder builder = http.put(ecdUrl)
		        .header(CHUNK_META_CONTAINER_ID,
		                oinf.url().cid())
		        .header(CHUNK_META_CONTENT_ID, oinf.oid())
		        .header(CHUNK_META_CONTENT_VERSION,
		                String.valueOf(oinf.version()))
		        .header(CHUNK_META_CONTENT_POLICY,
		                oinf.policy())
		        .header(CHUNK_META_CONTENT_MIME_TYPE,
		                oinf.mtype())
		        .header(CHUNK_META_CONTENT_CHUNK_METHOD,
		                oinf.chunkMethod())
		        .header(CHUNK_META_CONTENT_CHUNKSNB,
		                String.valueOf(oinf.nbchunks()))
		        .header(CHUNK_META_CONTENT_SIZE,
		                String.valueOf(oinf.metachunksize(pos)))
		        .header(CHUNK_META_CONTENT_PATH,
		                oinf.url().object())
		        .header(CHUNK_META_CHUNK_POS,
		                String.valueOf(pos))
		        .header(OioConstants.CHUNK_META_CHUNKS_NB,
		                String.valueOf(oinf.sortedChunks().get(pos).size()))
		        .header(OIO_REQUEST_ID_HEADER, reqId)
		        .body(data, size)
		        .verifier(RAWX_VERIFIER);

		for (ChunkInfo ci : oinf.sortedChunks().get(pos)) {
			builder.header(
			        OioConstants.CHUNK_META_CHUNK_PREFIX + ci.pos().sub(),
			        ci.url());
		}

		// TODO chunks hash

		builder.execute()
		        .close(false);
		return oinf;
	}
}