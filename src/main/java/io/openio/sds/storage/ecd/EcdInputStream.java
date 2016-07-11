package io.openio.sds.storage.ecd;

import static io.openio.sds.common.OioConstants.OIO_REQUEST_ID_HEADER;
import static io.openio.sds.http.Verifiers.RAWX_VERIFIER;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import io.openio.sds.common.OioConstants;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttp.RequestBuilder;
import io.openio.sds.http.OioHttpResponse;
import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ObjectInfo;

/**
 * For not rained items only
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class EcdInputStream extends InputStream {

	private static final SdsLogger logger = SdsLoggerFactory
	        .getLogger(EcdInputStream.class);

	private OioHttp http;
	private ObjectInfo oinf;
	private int pos = 0;
	private OioHttpResponse current;
	private String reqId;
	private String ecdUrl;
	private boolean eof = false;

	public EcdInputStream(String ecdUrl,
	        ObjectInfo oinf,
	        OioHttp http,
	        String reqId) {
		this.ecdUrl = ecdUrl;
		this.oinf = oinf;
		this.http = http;
		this.reqId = reqId;
	}

	@Override
	public void close() {
		if (null != current)
			current.close();
		pos = oinf.nbchunks() + 1;
	}

	@Override
	public int read() throws IOException {
		byte[] b = new byte[1];
		int read = read(b, 0, 1);
		return read < 0 ? -1 : b[0];
	}

	@Override
	public int read(byte[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}

	@Override
	public int read(byte[] buf, int offset, int length) throws IOException {
		if (0 >= length)
			return 0;
		int totRead = 0;
		while (totRead < length) {
			if (null == current || eof) {
				if (pos >= oinf.sortedChunks().size())
					return 0 == totRead ? -1 : totRead;
				next();
			}
			int read = current.body().read(buf, offset + totRead,
			        Math.min(length - totRead,
			                buf.length - offset + totRead));
			if (logger.isTraceEnabled())
				logger.trace("At offset 0+" + totRead + " of " + buf
						+ ", read byte " + buf[0] + " and length " + read
						+ " from " + current.body());
			if (-1 == read) {
				eof = true;
				current.close();
				current = null;
			} else {
				totRead += Math.max(0, read);
			}
		}
		return totRead;
	}

	private void next() {
		if (logger.isDebugEnabled())
			logger.debug("dl from " + ecdUrl);

		try {
			RequestBuilder builder = http.get(ecdUrl)
			        .header(OIO_REQUEST_ID_HEADER, reqId)
			        .header(OioConstants.CHUNK_META_CONTENT_CHUNK_METHOD,
			                oinf.chunkMethod())
			        .verifier(RAWX_VERIFIER);
			for (ChunkInfo ci : oinf.sortedChunks().get(pos)) {
				builder.header(
				        OioConstants.CHUNK_META_CHUNK_PREFIX + ci.pos().sub(),
				        ci.url());
			}

			builder.header(OioConstants.CHUNK_META_CHUNK_SIZE,
					oinf.sortedChunks().get(pos).get(0).size().toString());

			current = builder.execute();
			
			eof = false;
			pos++;
		} catch (OioException e) {
			logger.warn(String.format(
			        "Error while trying to download pos %d from %s",
			        pos,
			        ecdUrl),
			        e);
			throw e;
		}
	}
}
