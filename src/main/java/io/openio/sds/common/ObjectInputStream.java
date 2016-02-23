package io.openio.sds.common;

import java.io.IOException;
import java.io.InputStream;

import io.openio.sds.RawxClient;
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
    private OioHttpResponse current;

    public ObjectInputStream(ObjectInfo oinf, OioHttp http) {
        this.oinf = oinf;
        this.http = http;
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
        if (null == current || 0 >= currentRemaining) {
            if (pos >= oinf.nbchunks())
                return -1;
            next(0);
            return read(buf, offset, length);
        }
        int read = current.body().read(buf, offset, Math.min(currentRemaining,
                Math.min(length, buf.length - offset)));
        if (-1 == read)
            throw new IOException("Unexpected end of chunk stream");
        currentRemaining -= read;
        if (0 == currentRemaining) {
            current.close();
            current = null;
        }
        if (read < length)
            return read + Math.max(0, read(buf, offset + read, length - read));
        return read;
    }

    private void next(int offset) {
        ChunkInfo ci = oinf.sortedChunks().get(pos).get(offset);
        if (logger.isDebugEnabled())
            logger.debug("dl from " + ci.url());
        try {
            current = http.get(ci.url())
                    .verifier(RawxClient.RAWX_VERIFIER)
                    .execute();
            currentRemaining = ci.size().intValue();
            pos++;
        } catch (OioException e) {
            if (offset + 1 >= oinf.sortedChunks().get(pos).size())
                throw new OioException(
                        "Definitly fail to download chunk at pos " + pos, e);
            logger.warn("Error while trying to download " + ci.url(), e);
            next(offset + 1);
        }
    }

}
