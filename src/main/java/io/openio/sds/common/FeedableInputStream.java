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
 *
 */
public class FeedableInputStream extends InputStream {

    private static final SdsLogger logger = SdsLoggerFactory
            .getLogger(FeedableInputStream.class);

    private LinkedBlockingQueue<DataPart> q;
    private DataPart current = null;

    public FeedableInputStream(int qsize) {
        this.q = new LinkedBlockingQueue<FeedableInputStream.DataPart>(5);
    }

    public void feed(ByteBuffer b, boolean last) {
        try {
            q.put(new DataPart(b, last));
        } catch (InterruptedException e) {
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
        if (0 >= length)
            return 0;
        if (null == current) {
            try {
                current = q.poll(10L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                current = new DataPart(null, true);
            }
            return read(buf, offset, length);
        }
        
        if (!current.buffer().hasRemaining()) {
            if (current.isLast())
                return -1;
            current = null;
            return read(buf, offset, length);
        }
        int read = Math.min(current.buffer().remaining(),
                Math.min(buf.length - offset, length));
        current.buffer().get(buf, offset, read);
        if (read < length)
            return read + Math.max(0, read(buf, offset + read, length - read));
        return read;
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
