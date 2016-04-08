package io.openio.sds.common;

import static io.openio.sds.common.OioConstants.OIO_REQUEST_ID_HEADER;
import static io.openio.sds.http.Verifiers.RAWX_VERIFIER;

import java.io.IOException;
import java.io.InputStream;

import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttp;
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
public class ObjectInputStream extends InputStream {

    private static final SdsLogger logger = SdsLoggerFactory
            .getLogger(ObjectInputStream.class);

    private OioHttp http;
    private ObjectInfo oinf;
    private int pos = 0;
    private int currentRemaining;
    private ChunkInfo currentChunk;
    private OioHttpResponse current;
    private String reqId;

    public ObjectInputStream(ObjectInfo oinf, OioHttp http, String reqId) {
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
            if (null == current || 0 >= currentRemaining) {
                if (pos >= oinf.nbchunks())
                    return 0 == totRead ? -1 : totRead;
                next(0);
            }
            int read = current.body().read(buf, offset + totRead,
                    Math.min(currentRemaining,
                            Math.min(length - totRead, buf.length - offset + totRead)));
            if (-1 == read)
                throw new IOException(
                        String.format(
                                "Error during download, unexpected end of chunk stream (url: %s, read: %d, size: %d)",
                                currentChunk.url(),
                                currentChunk.size() - currentRemaining,
                                currentChunk.size()));
            currentRemaining -= read;
            if (0 == currentRemaining) {
                current.close();
                current = null;
            }
            totRead += Math.max(0, read);
        }
        return totRead;
    }

    private void next(int offset) {
        currentChunk = oinf.sortedChunks().get(pos).get(offset);
        if (logger.isDebugEnabled())
            logger.debug("dl from " + currentChunk.url());
        try {
            current = http.get(currentChunk.url())
                    .header(OIO_REQUEST_ID_HEADER, reqId)
                    .verifier(RAWX_VERIFIER)
                    .execute();
            currentRemaining = currentChunk.size().intValue();
            pos++;
        } catch (OioException e) {
            if (offset + 1 >= oinf.sortedChunks().get(pos).size())
                throw new OioException(
                        "Definitly fail to download chunk at pos " + pos, e);
            logger.warn("Error while trying to download " + currentChunk.url(),
                    e);
            next(offset + 1);
        }
    }

}
