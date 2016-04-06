package io.openio.sds.rawx;

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
import static io.openio.sds.common.OioConstants.OIO_REQUEST_ID_HEADER;
import static io.openio.sds.http.Verifiers.RAWX_VERIFIER;
import static java.nio.ByteBuffer.wrap;

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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.openio.sds.common.FeedableInputStream;
import io.openio.sds.common.ObjectInputStream;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttp.RequestBuilder;
import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ObjectInfo;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class RawxClient {

    private static final SdsLogger logger = SdsLoggerFactory
            .getLogger(RawxClient.class);

    private static final int MIN_WORKERS = 1;
    private static final int MAX_WORKERS = 100;
    private static final int IDLE_THREAD_KEEP_ALIVE = 30; // in seconds

    private final OioHttp http;
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

    /**
     * Uploads the chunks of the specified {@code ObjectInfo} asynchronously
     * 
     * 
     * @param oinf
     *            the ObjectInfo to deal with
     * @param data
     *            the data to upload
     * 
     * @return oinf
     */
    public ObjectInfo uploadChunks(ObjectInfo oinf,
            InputStream data) {
        return uploadChunks(oinf, data, requestId());
    }

    /**
     * Uploads the chunks of the specified {@code ObjectInfo} asynchronously
     * 
     * @param oinf
     *            the ObjectInfo to deal with
     * @param data
     *            the data to upload
     * @param reqId
     *            the id to use to identify the request
     * @return oinf
     */
    public ObjectInfo uploadChunks(ObjectInfo oinf,
            InputStream data, String reqId) {
        long remaining = oinf.size();
        for (int pos = 0; pos < oinf.nbchunks(); pos++) {
            long csize = Math.min(remaining, oinf.chunksize(pos));
            uploadPosition(oinf, pos, csize, data, reqId);
            remaining -= csize;
        }
        return oinf;
    }

    /**
     * Uploads the chunks of the specified {@code ObjectInfo} asynchronously
     * 
     * @param oinf
     *            the ObjectInfo to deal with
     * @param data
     *            the data to upload
     * @return oinf
     */
    public ObjectInfo uploadChunks(ObjectInfo oinf, File data) {
        return uploadChunks(oinf, data, requestId());
    }

    /**
     * Uploads the chunks of the specified {@code ObjectInfo} asynchronously
     * 
     * @param oinf
     *            the ObjectInfo to deal with
     * @param data
     *            the data to upload
     * @param reqId
     *            the id to use to identify the request
     * @return oinf
     */
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

    public ObjectInfo uploadChunks(ObjectInfo oinf, byte[] data) {
        return uploadChunks(oinf, data, requestId());
    }

    public ObjectInfo uploadChunks(
            ObjectInfo oinf, byte[] data, String reqId) {
        return uploadChunks(oinf, new ByteArrayInputStream(data), reqId);
    }

    public InputStream downloadObject(ObjectInfo oinf) {
        return downloadObject(oinf, requestId());
    }
    
    public InputStream downloadObject(ObjectInfo oinf, String reqId) {
        checkArgument(null != oinf);
        return new ObjectInputStream(oinf, http, reqId);
    }

    /* --- INTERNALS --- */

    private ObjectInfo uploadPosition(final ObjectInfo oinf,
            final int pos, final Long size, InputStream data,
            final String reqId) {
        List<ChunkInfo> cil = oinf.sortedChunks().get(pos);
        final List<FeedableInputStream> gens = size == 0 ? null
                : feedableBodys(cil.size(), size);
        List<Future<OioException>> futures = new ArrayList<Future<OioException>>();
        for (int i = 0; i < cil.size(); i++) {
            final ChunkInfo ci = cil.get(i);
            final FeedableInputStream in = null == gens ? null : gens.get(i);

            futures.add(executors.submit(new Callable<OioException>() {

                @Override
                public OioException call() {
                    try {
                        RequestBuilder builder = http.put(ci.url())
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
                                        String.valueOf(oinf.size()))
                                .header(CHUNK_META_CONTENT_PATH,
                                        oinf.url().object())
                                .header(CHUNK_META_CHUNK_ID, ci.id())
                                .header(CHUNK_META_CHUNK_POS,
                                        ci.pos().toString())
                                .header(OIO_REQUEST_ID_HEADER, reqId)
                                .verifier(RAWX_VERIFIER);
                        if (null == gens)
                            builder.body("");
                        else
                            builder.body(in, size);
                        ci.size(size);
                        ci.hash(builder.execute()
                                .close(false)
                                .header(CHUNK_META_CHUNK_HASH));
                    } catch (OioException e) {
                        return e;
                    }
                    return null;
                }
            }));
        }
        consume(data, size, gens, futures);
        try {
            for (Future<OioException> f : futures) {
                OioException e;
                try {
                    e = f.get(60, TimeUnit.SECONDS);
                } catch (TimeoutException e1) {
                    e = new OioException("Chunk upload timeout", e1);
                }
                // TODO improve, we should cry only in case of all copy fails
                if (null != e)
                    throw e;
            }
        } catch (InterruptedException e) {
            throw new OioException("get interrupted", e);
        } catch (ExecutionException e) {
            throw new OioException("Execution exception", e.getCause());
        }
        return oinf;
    }

    private void consume(InputStream data, Long size,
            List<FeedableInputStream> gens,
            List<Future<OioException>> futures) {
        int done = 0;
        while (done < size) {
            byte[] b = new byte[Math.min(size.intValue() - done,
                    settings.http().receiveBufferSize())];
            try {
                done += fill(b, data);
                for (FeedableInputStream in : gens) {
                    in.feed(wrap(b), done >= size);
                }
            } catch (IOException e) {
                logger.error(e);
                for (Future<OioException> f : futures)
                    f.cancel(true);
                throw new OioException("Stream consumption error", e);
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

    private List<FeedableInputStream> feedableBodys(int count,
            long size) {
        ArrayList<FeedableInputStream> res = new ArrayList<FeedableInputStream>();
        for (int i = 0; i < count; i++)
            res.add(new FeedableInputStream(5));
        return res;
    }
}
