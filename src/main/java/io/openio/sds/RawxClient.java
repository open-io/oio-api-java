package io.openio.sds;

import static io.openio.sds.common.Check.checkArgument;
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
import static java.lang.String.format;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.openio.sds.common.FeedableInputStream;
import io.openio.sds.common.ObjectInputStream;
import io.openio.sds.exceptions.BadRequestException;
import io.openio.sds.exceptions.ChunkNotFoundException;
import io.openio.sds.exceptions.SdsException;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttp.RequestBuilder;
import io.openio.sds.http.OioHttpResponse;
import io.openio.sds.http.OioHttpResponseVerifier;
import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.settings.RawxSettings;

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
    private static final int BACKLOG_MAX_SIZE = 10 * MAX_WORKERS;

    private final OioHttp http;
    private final ExecutorService executors;
    private final RawxSettings settings;

    RawxClient(OioHttp http, RawxSettings settings) {
        this.http = http;
        this.settings = settings;
        this.executors = new ThreadPoolExecutor(MIN_WORKERS,
                MAX_WORKERS,
                IDLE_THREAD_KEEP_ALIVE,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(BACKLOG_MAX_SIZE),
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
     * @param oinf
     *            the ObjectInfo to deal with
     * @param data
     *            the data to upload
     * @param listener
     *            the {@link UploadListener} to check progression
     * @return a ListenableFuture which handles the updated {@code ObjectInfo}
     */
    public ObjectInfo uploadChunks(ObjectInfo oinf,
            InputStream data) {
        long remaining = oinf.size();
        for (int pos = 0; pos < oinf.nbchunks(); pos++) {
            long csize = Math.min(remaining, oinf.chunksize(pos));
            uploadPosition(oinf, pos, csize, data);
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
     * @param listener
     *            the {@link UploadListener} to check progression
     * @return a ListenableFuture which handles the updated {@code ObjectInfo}
     */
    public ObjectInfo uploadChunks(ObjectInfo oinf, File data) {
        try {
            return uploadChunks(oinf, new FileInputStream(data));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found", e);
        }
    }

    public ObjectInfo uploadChunks(
            ObjectInfo oinf, byte[] data) {
        return uploadChunks(oinf, new ByteArrayInputStream(data));
    }

    public InputStream downloadObject(ObjectInfo oinf) {
        checkArgument(null != oinf);
        return new ObjectInputStream(oinf, http);
    }

    /* --- INTERNALS --- */

    private ObjectInfo uploadPosition(final ObjectInfo oinf,
            final int pos, final Long size, InputStream data) {
        List<ChunkInfo> cil = oinf.sortedChunks().get(pos);
        final List<FeedableInputStream> gens = size == 0 ? null
                : feedableBodys(cil.size(), size);
        Future<OioHttpResponse>[] futures = new Future[cil.size()];
        for (int i = 0; i < cil.size(); i++) {
            final ChunkInfo ci = cil.get(i);
            final FeedableInputStream in = null == gens ? null : gens.get(i);
            futures[i] = executors.submit(new Callable<OioHttpResponse>() {

                @Override
                public OioHttpResponse call() throws Exception {
                    RequestBuilder builder = http.put(ci.url())
                            .header(CHUNK_META_CONTAINER_ID, oinf.url().cid())
                            .header(CHUNK_META_CONTENT_ID, oinf.oid())
                            .header(CHUNK_META_CONTENT_POLICY, oinf.policy())
                            .header(CHUNK_META_CONTENT_MIME_TYPE, oinf.mtype())
                            .header(CHUNK_META_CONTENT_CHUNK_METHOD,
                                    oinf.chunkMethod())
                            .header(CHUNK_META_CONTENT_CHUNKSNB,
                                    String.valueOf(oinf.nbchunks()))
                            .header(CHUNK_META_CONTENT_SIZE,
                                    String.valueOf(oinf.size()))
                            .header(CHUNK_META_CONTENT_PATH,
                                    oinf.url().object())
                            .header(CHUNK_META_CHUNK_ID, ci.id())
                            .header(CHUNK_META_CHUNK_POS, ci.pos().toString())
                            .verifier(RAWX_VERIFIER);
                    if (null == gens)
                        builder.body("");
                    else
                        builder.body(in, size);
                    return builder.execute();
                }

            });
        }

        consume(data, size, gens, futures);

        for (int i = 0; i < futures.length; i++) {
            try {
                OioHttpResponse resp = futures[i].get().close();
                cil.get(i).hash(resp.header(CHUNK_META_CHUNK_HASH));
            } catch (InterruptedException e) {
                throw new SdsException("get interrupted", e);
            } catch (ExecutionException e) {
                throw new SdsException("Execution exception", e.getCause());
            }

        }

        return oinf;
    }

    private void consume(InputStream data, Long size,
            List<FeedableInputStream> gens,
            Future<OioHttpResponse>[] futures) {
        int done = 0;
        while (done < size) {
            byte[] b = new byte[Math.min(size.intValue() - done,
                    settings.bufsize())];
            try {
                done += fill(b, data);
                for (FeedableInputStream in : gens) {
                    in.feed(wrap(b), done >= size);
                }
            } catch (IOException e) {
                logger.error(e);
                for (Future<OioHttpResponse> f : futures)
                    f.cancel(true);
                throw new SdsException("Stream consumption error", e);
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

    public static final OioHttpResponseVerifier RAWX_VERIFIER = new OioHttpResponseVerifier() {

        @Override
        public void verify(OioHttpResponse resp) throws SdsException {
            switch (resp.code()) {
            case 200:
            case 201:
            case 204:
                return;
            case 400:
                throw new BadRequestException(resp.msg());
            case 404:
                throw new ChunkNotFoundException(resp.msg());
            case 500:
                throw new SdsException(format("Internal error (%d %s)",
                        resp.code(), resp.msg()));
            default:
                throw new SdsException(format("Unmanaged response code (%d %s)",
                        resp.code(), resp.msg()));
            }
        }
    };
}
