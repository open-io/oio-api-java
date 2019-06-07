package io.openio.sds.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;

/**
 *
 * @author Christopher Dedeurwaerder
 * @author Florent Vennetier
 */
public class FeedableInputStream extends InputStream {

    private static final SdsLogger logger = SdsLoggerFactory
            .getLogger(FeedableInputStream.class);

    private LinkedBlockingQueue<DataPart> q;
    private DataPart current = null;
    private boolean failed = false;
    private long pollDelayMillis = 10000;

    /**
     * @param qsize Queue size
     * @param pollDelayMillis Delay between iterations of the read loop
     * @param retries Maximum number of iterations of the read loop
     */
    public FeedableInputStream(int qsize, long pollDelayMillis, int retries) {
        this.q = new LinkedBlockingQueue<FeedableInputStream.DataPart>(qsize);
        this.pollDelayMillis = pollDelayMillis;
    }

    public FeedableInputStream(int qsize, long pollDelayMillis) {
        this(qsize, pollDelayMillis, 3);
    }

    public FeedableInputStream(int qsize) {
        this(qsize, 10000);
    }

    public void setFailed(boolean f) {
        failed = f;
    }

    public boolean isFailed() {
        return failed;
    }

    public void feed(ByteBuffer b, boolean last) {
        if (failed)
            return;
        try {
            DataPart part = new DataPart(b, last);
            while (!(failed || q.offer(part, 1L, TimeUnit.SECONDS))) {
                /* Retry until done
                 * or reader has been interrupted
                 * or we are interrupted */
                logger.debug("Timed out while trying to feed queue, " +
                        "will check if not failed and then retry");
            }
        } catch (InterruptedException e) {
            failed = true;
            logger.warn("feed interrupted", e);
        }
    }

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        int read = read(b, 0, 1);
        return -1 == read ? -1 : b[0];
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    @Override
    public int read(byte[] buf, int offset, int length) {
        if (length <= 0)
            return 0;
        int retriesLeft = 5;
        while (current == null && retriesLeft > 0) {
            try {
                current = q.poll(this.pollDelayMillis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                failed = true;
                current = new DataPart(null, true);
            }
            retriesLeft--;
            if (current == null) {
                logger.warn("Failed to read from client application, " +
                        retriesLeft + " retries left");
            } else if (current.buffer() == null ||
                    !current.buffer().hasRemaining()) {
                if (current.isLast())
                    return -1;
                current = null;
            }
        }

        if (current == null) {
            failed = true;
            return -1;
        }

        int read = Math.min(current.buffer().remaining(),
                Math.min(buf.length - offset, length));
        current.buffer().get(buf, offset, read);

        int rc;
        if (read < length) {
            rc = read + Math.max(0, read(buf, offset + read, length - read));
        } else {
            rc = read;
        }

        if (current.buffer().remaining() == 0)
            current = null;
        return rc;
    }

    public static class DataPart {

        private ByteBuffer buffer;
        private boolean last = false;

        public DataPart(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        public DataPart(ByteBuffer buffer, boolean last) {
            this.buffer = buffer;
            this.last = last;
        }

        public ByteBuffer buffer() {
            return buffer;
        }

        public boolean isLast() {
            return last;
        }

    }

}
