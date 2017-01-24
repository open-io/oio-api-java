package io.openio.sds;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream} wrapped that stops reading prematurely.
 *
 * @author Florent Vennetier
 */
public class MissingByteInputStream extends InputStream {

    private InputStream wrapped = null;
    private int readAttempts = 0;
    private long bytesLeft = 0;
    private long delay = 1000;

    /**
     * @param wrapped
     *            {@link InputStream} to wrap
     * @param readLimit
     *            Maximum number of bytes to read/skip from the wrapped
     *            {@link InputStream}
     * @param readAttempts
     *            Number of read attempts before returning EOF
     * @param delay
     *            Delay in milliseconds before return 0 or EOF (does not apply
     *            on successful reads)
     */
    public MissingByteInputStream(InputStream wrapped, long readLimit, int readAttempts,
            long delay) {
        super();
        this.wrapped = wrapped;
        this.readAttempts = readAttempts;
        this.bytesLeft = readLimit;
        this.delay = delay;
    }

    public MissingByteInputStream(InputStream wrapped, long readLimit, int readAttempts) {
        this(wrapped, readLimit, readAttempts, 1000);
    }

    public MissingByteInputStream(InputStream wrapped, long readLimit) {
        this(wrapped, readLimit, 0);
    }

    private long readLimit(long wanted) {
        return Math.min(wanted, bytesLeft);
    }

    public int readAttemptsLeft() {
        return this.readAttempts;
    }

    @Override
    public int read() throws IOException {
        byte[] buf = new byte[1];
        int rc = this.read(buf, 0, 1);
        if (rc > 0)
            return buf[0];
        return rc;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        long readLimit = readLimit(len);
        if (readLimit <= 0) {
            this.readAttempts--;
            if (readAttemptsLeft() >= 0) {
                try {
                    System.out.println("Sleeping " + this.delay +
                            "ms before returning 0 bytes");
                    Thread.sleep(this.delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 0;
            } else {
                // Simulate end-of-file
                System.out.println("Sleeping " + this.delay +
                        "ms before Simulating premature end-of-file");
                try {
                    Thread.sleep(this.delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return -1;
            }
        }
        int rc = this.wrapped.read(b, off, (int)readLimit);
        if (rc > 0)
            this.bytesLeft -= rc;
        return rc;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n <= 0)
            return 0;
        long rc = wrapped.skip(readLimit(n));
        this.bytesLeft -= rc;
        return rc;
    }

    @Override
    public int available() throws IOException {
        return wrapped.available();
    }

    @Override
    public void close() throws IOException {
        wrapped.close();
    }

}
