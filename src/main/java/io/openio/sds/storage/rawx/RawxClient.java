package io.openio.sds.storage.rawx;

import io.openio.sds.RequestContext;
import io.openio.sds.common.FeedableInputStream;
import io.openio.sds.common.Hex;
import io.openio.sds.common.Strings;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttp.RequestBuilder;
import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.Range;
import io.openio.sds.storage.DownloadHelper;
import io.openio.sds.storage.StorageClient;
import io.openio.sds.storage.Target;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.openio.sds.common.Check.checkArgument;
import static io.openio.sds.common.IdGen.requestId;
import static io.openio.sds.common.OioConstants.CHUNK_META_CHUNK_HASH;
import static io.openio.sds.common.OioConstants.CHUNK_META_CHUNK_ID;
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
import static io.openio.sds.common.OioConstants.CHUNK_META_FULL_PATH;
import static io.openio.sds.common.OioConstants.CHUNK_META_OIO_VERSION;
import static io.openio.sds.common.OioConstants.OIO_REQUEST_ID_HEADER;
import static io.openio.sds.http.Verifiers.RAWX_VERIFIER;
import static java.lang.String.format;
import static java.nio.ByteBuffer.wrap;

/**
 * @author Christopher Dedeurwaerder
 */
public class RawxClient implements StorageClient {

	private static final SdsLogger logger = SdsLoggerFactory
			.getLogger(RawxClient.class);

	private static final int MIN_WORKERS = 1;
	private static final int MAX_WORKERS = 100;
	private static final int IDLE_THREAD_KEEP_ALIVE = 30; // in seconds

	final OioHttp http;
	private final ExecutorService executors;
	private final RawxSettings settings;

	public RawxClient(OioHttp http, RawxSettings settings) {
		this.http = http;
		this.settings = settings;
		this.executors = new ThreadPoolExecutor(MIN_WORKERS,
				MAX_WORKERS,
				IDLE_THREAD_KEEP_ALIVE,
				TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(),
				new ThreadFactory() {

					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						t.setName("RawxClient-Worker");
						return t;
					}
				});
	}

	public static RawxClient client(OioHttp http,
									RawxSettings settings) {
		checkArgument(null != http, "AsynHttpClient cannot be null");
		checkArgument(null != settings, "Settings cannot be null");
		return new RawxClient(http, settings);
	}

	public int getActiveUploadCount() {
		return ((ThreadPoolExecutor) this.executors).getActiveCount();
	}

	/**
	 * Uploads the chunks of the specified {@code ObjectInfo} asynchronously
	 *
	 * @param oinf the ObjectInfo to deal with
	 * @param data the data to upload
	 * @return {@code oinf}
	 */
	public ObjectInfo uploadChunks(ObjectInfo oinf,
								   InputStream data) {
		return uploadChunks(oinf, data, new RequestContext());
	}

	/**
	 * Uploads the chunks of the specified {@code ObjectInfo} asynchronously
	 *
	 * @param oinf  the ObjectInfo to deal with
	 * @param data  the data to upload
	 * @param reqCtx Common parameters to all requests
	 * @return {@code oinf}
	 */
	public ObjectInfo uploadChunks(ObjectInfo oinf,
								   InputStream data, RequestContext reqCtx) {
		StreamWrapper wrapper = new StreamWrapper(data);
		long remaining = oinf.size();
		for (int pos = 0; pos < oinf.nbchunks(); pos++) {
			long csize = Math.min(remaining, oinf.chunksize(pos));
			if (csize == 0 && pos != 0)
				throw new OioException("Too many chunks prepared");
			uploadPosition(oinf, pos, csize, wrapper, reqCtx);
			remaining -= csize;
		}
		return oinf.hash(Hex.toHex(wrapper.md5()));
	}

	/**
	 * Uploads the chunks of the specified {@code ObjectInfo} asynchronously
	 *
	 * @param oinf the ObjectInfo to deal with
	 * @param data the data to upload
	 * @return oinf
	 */
	public ObjectInfo uploadChunks(ObjectInfo oinf, File data) {
		return uploadChunks(oinf, data, new RequestContext());
	}

	/**
	 * Uploads the chunks of the specified {@code ObjectInfo} asynchronously
	 *
	 * @param oinf  the ObjectInfo to deal with
	 * @param data  the data to upload
	 * @param reqCtx Common parameters to all requests
	 * @return oinf
	 */
	public ObjectInfo uploadChunks(ObjectInfo oinf, File data, RequestContext reqCtx) {

		try {
			FileInputStream fin = new FileInputStream(data);
			try {
				return uploadChunks(oinf, fin, reqCtx);
			} finally {
				try {
					fin.close();
				} catch (IOException e) {
					logger.warn("Failed to close Inputstream, possible leak", e);
				}
			}
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File not found", e);
		}
	}

	public ObjectInfo uploadChunks(ObjectInfo oinf, byte[] data) {
		return uploadChunks(oinf, data, new RequestContext());
	}

	public ObjectInfo uploadChunks(
			ObjectInfo oinf, byte[] data, RequestContext reqCtx) {
		return uploadChunks(oinf, new ByteArrayInputStream(data), reqCtx);
	}

	public InputStream downloadObject(ObjectInfo oinf) {
		return downloadObject(oinf, new RequestContext());
	}

	public InputStream downloadObject(ObjectInfo oinf, Range range) {
		return downloadObject(oinf, range, new RequestContext());
	}

	public InputStream downloadObject(ObjectInfo oinf, RequestContext reqCtx) {
		return downloadObject(oinf, null, reqCtx);
	}

	public InputStream downloadObject(ObjectInfo oinf, Range range, RequestContext reqCtx) {
		checkArgument(null != oinf);
		List<Target> targets = DownloadHelper.loadTargets(oinf, range);
		return new ObjectInputStream(targets, http, reqCtx);
	}

	public void deleteChunks(List<ChunkInfo> l) {
		for (ChunkInfo ci : l)
			deleteChunk(ci);
	}

	public void deleteChunk(ChunkInfo ci) {
		// no verifier, suppress exceptions
		try {
			http.delete(ci.finalUrl())
					.execute()
					.close();
		} catch (OioException e) {
			if (logger.isDebugEnabled())
				logger.debug(format("Chunk %s deletion error", ci.url()), e);
		}
	}

	/* --- INTERNALS --- */

	private <E> void quorumOrFail(int pos, int quorum, List<E> successes) {
		if (successes.size() < quorum) {
			throw new OioException(format(
					"Quorum not reached when writing chunks at position %s (%d/%d)",
					pos, successes.size(), quorum));
		}
	}

	private void cancelTasks(List<Future<UploadResult>> futures, Exception exc,
							 String prefixMessage) {
		int notTerminated = 0;
		for (Future<UploadResult> future : futures) {
			if (!future.isDone())
				notTerminated++;
			future.cancel(true);
		}
		String message = prefixMessage;
		if (notTerminated > 0)
			message += " (" + notTerminated + " upload jobs cancelled)";
		throw new OioException(message, exc);
	}

	private ObjectInfo uploadPosition(final ObjectInfo oinf, final int pos, final Long size,
									  InputStream data, final RequestContext reqCtx) {
		List<ChunkInfo> cil = oinf.sortedChunks().get(pos);
		final List<FeedableInputStream> gens = size == 0 ? null : feedableBodies(cil.size(), size);
		List<Future<UploadResult>> futures = new ArrayList<Future<UploadResult>>();

		int quorum;
		if (!settings.quorumWrite())
			quorum = cil.size();
		else
			quorum = (cil.size() + 1) / 2;

		for (int i = 0; i < cil.size(); i++) {
			final ChunkInfo ci = cil.get(i);
			final FeedableInputStream in = null == gens ? null : gens.get(i);

			Callable<UploadResult> uploader = new Callable<UploadResult>() {

				@Override
				public UploadResult call() {
					UploadResult result = new UploadResult(ci);
					try {
						RequestBuilder builder = http
								.put(ci.finalUrl())
								.header(CHUNK_META_CONTAINER_ID, oinf.url().cid())
								.header(CHUNK_META_CONTENT_ID, oinf.oid())
								.header(CHUNK_META_CONTENT_VERSION, String.valueOf(oinf.version()))
								.header(CHUNK_META_CONTENT_POLICY, oinf.policy())
								.header(CHUNK_META_CONTENT_MIME_TYPE, oinf.mimeType())
								.header(CHUNK_META_CONTENT_CHUNK_METHOD, oinf.chunkMethod())
								.header(CHUNK_META_CONTENT_CHUNKSNB,
										String.valueOf(oinf.nbchunks()))
								.header(CHUNK_META_CONTENT_SIZE, String.valueOf(oinf.size()))
								.header(CHUNK_META_CONTENT_PATH, Strings.quote(oinf.url().object()))
								.header(CHUNK_META_CHUNK_ID, ci.id())
								.header(CHUNK_META_CHUNK_POS, ci.pos().toString())
								.header(CHUNK_META_FULL_PATH, oinf.fullpath())
								.header(CHUNK_META_OIO_VERSION, "4")
								.verifier(RAWX_VERIFIER)
								.withRequestContext(reqCtx);
						if (null == gens)
							builder.body("");
						else
							builder.body(in, size);
						ci.size(size);
						ci.hash(builder.execute().close(false).header(CHUNK_META_CHUNK_HASH));
					} catch (OioException e) {
						in.setFailed(true);
						result.exception(e);
					}
					return result;
				}
			};
			int retry = 0;
			try {
				while (true) {
					try {
						Future<UploadResult> upload = executors.submit(uploader);
						futures.add(upload);
						break;
					} catch (RejectedExecutionException ree) {
						if (retry < 5) {
							int delay = 1 << retry;
							logger.warn("Failed to start chunk upload, retry in " + delay + "s",
									ree);
							try {
								Thread.sleep(delay * 1000);
							} catch (InterruptedException e) {
								UploadResult result = new UploadResult(ci);
								OioException exc = new OioException(
										"Failed to retry chunk upload", e);
								result.exception(exc);
								throw exc;
							}
						} else {
							UploadResult result = new UploadResult(ci);
							OioException exc = new OioException(
									"Failed to schedule chunk upload", ree);
							result.exception(exc);
							throw exc;
						}
						retry++;
					}
				}
			} catch (RuntimeException e) {
				try {
					in.close();
				} catch (IOException e1) {
					logger.warn(e1);
				}
			}
		}
		try {
			quorumOrFail(pos, quorum, futures);
		} catch (Exception e) {
			cancelTasks(futures, e,
						"Too many failures to schedule chunk uploads");
		}

		try {
			consume(data, size, gens, futures);
		} catch (Exception e) {
			cancelTasks(futures, e, "Stream read error");
		}

		try {
			List<UploadResult> successes = new ArrayList<UploadResult>();
			for (Future<UploadResult> future : futures) {
				UploadResult result = future.get();

				if (null != result.exception()) {
					logger.warn(format("Failed to upload chunk %s", result.chunkInfo()), result.exception());
				} else {
					successes.add(result);
				}
			}
			quorumOrFail(pos, quorum, successes);
		} catch (InterruptedException e) {
			throw new OioException("got interrupted", e);
		} catch (ExecutionException e) {
			throw new OioException("Execution exception", e.getCause());
		}
		return oinf;
	}

	private void consume(InputStream data, Long size,
						 List<FeedableInputStream> gens,
						 List<Future<UploadResult>> futures) throws IOException {
		int done = 0;
		while (done < size) {
			byte[] b = new byte[Math.min(size.intValue() - done,
					settings.http().receiveBufferSize())];
			done += fill(b, data);
			for (FeedableInputStream in : gens) {
				in.feed(wrap(b), done >= size);
			}
		}
	}

	private int fill(byte[] b, InputStream data) throws IOException {
		int done = 0;
		int read = 0;
		while (done < b.length) {
			read = data.read(b, done, b.length - done);
			if (-1 == read)
				throw new EOFException("Unexpected end of stream");
			done += read;
		}
		return done;
	}

	private List<FeedableInputStream> feedableBodies(int count,
													 long size) {
		ArrayList<FeedableInputStream> res = new ArrayList<FeedableInputStream>();
		for (int i = 0; i < count; i++)
			res.add(new FeedableInputStream(5, settings.http().readTimeout() / 5, 5));
		return res;
	}
}
