package io.openio.sds.storage.rawx;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class StreamWrapper extends InputStream {

    private static final SdsLogger logger = SdsLoggerFactory
            .getLogger(StreamWrapper.class);

    private final InputStream in;
    private MessageDigest digest;

    public StreamWrapper(InputStream in) {
        this.in = in;
        try {
            this.digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Unable to compute MD5", e);
            this.digest = null;
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public int read() throws IOException {
        int res = in.read();
        if (null != digest)
            digest.update((byte) res);
        return res;
    }

    @Override
    public int read(byte[] buf) throws IOException {
        int res = in.read(buf);
        if (res > 0 && null != digest)
            digest.update(buf, 0, res);
        return res;
    }

    @Override
    public int read(byte[] buf, int offset, int len) throws IOException {
        int res = in.read(buf, offset, len);
        if (res > 0 && null != digest)
            digest.update(buf, offset, res);
        return res;
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    @Override
    public void reset() throws IOException {
        in.reset();
    }

    public byte[] md5() {
        return null != digest ? digest.digest() : null;
    }

}
