package io.openio.sds.storage.rawx;

import io.openio.sds.common.OioConstants;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttp.RequestBuilder;
import io.openio.sds.http.OioHttpResponse;
import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.storage.Target;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static io.openio.sds.common.OioConstants.OIO_REQUEST_ID_HEADER;
import static io.openio.sds.http.Verifiers.RAWX_VERIFIER;
import static java.lang.String.format;

/**
 * For replicated policies only
 *
 * @author Christopher Dedeurwaerder
 */
public class ObjectInputStream extends InputStream {

	private static final SdsLogger logger = SdsLoggerFactory
			.getLogger(ObjectInputStream.class);

	private OioHttp http;
	private List<Target> targets;
	private int pos = 0;
	private long currentRemaining;
	private ChunkInfo currentChunk;
	private OioHttpResponse current;
	private String reqId;

	public ObjectInputStream(List<Target> targets, OioHttp http, String reqId) {
		this.targets = targets;
		this.http = http;
		this.reqId = reqId;
	}

	@Override
	public void close() {
		if (null != current)
			current.close();
		pos = targets.size() + 1;
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
			if (null == current || 0 >= currentRemaining) {
				if (pos >= targets.size())
					return 0 == totRead ? -1 : totRead;
				next(0);
			}

			int read = current.body().read(buf, offset + totRead,
					Math.min(remaining(),
							Math.min(length - totRead,
									buf.length - offset + totRead)));

			if (currentRemaining != 0) {
				if (-1 == read) {
					throw new IOException(
							format(
									"Error during download, unexpected end of chunk stream (url: %s, read: %d, size: %d)",
									currentChunk.url(),
									currentChunk.size() - currentRemaining,
									currentChunk.size()));
				}

				currentRemaining -= read;
			}
			if (0 == currentRemaining) {
				current.close();
				current = null;
			}
			totRead += Math.max(0, read);
		}
		return totRead;
	}

	private int remaining() {
		return currentRemaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) currentRemaining;
	}

	private void next(int offset) {
		Target t = targets.get(pos);
		currentChunk = t.getChunk().get(offset);
		if (logger.isDebugEnabled())
			logger.debug("download from " + currentChunk.url());
		try {
			RequestBuilder builder = http.get(currentChunk.url())
					.header(OIO_REQUEST_ID_HEADER, reqId)
					.verifier(RAWX_VERIFIER);

			if (null != targets.get(pos).getRange())
				builder.header(OioConstants.RANGE_HEADER,
						targets.get(pos).getRange().headerValue());

			current = builder.execute();

			currentRemaining = null != targets.get(pos).getRange()
					? t.getRange().to() - t.getRange().from()
					: currentChunk.size().intValue();
			pos++;
		} catch (OioException e) {
			if (offset + 1 >= targets.get(pos).getChunk().size())
				throw new OioException(
						"Definitely failed to download chunk at pos " + pos, e);
			logger.warn("Error while trying to download " + currentChunk.url(),
					e);
			next(offset + 1);
		}
	}

}
