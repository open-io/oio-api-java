package io.openio.sds.http;

import java.io.IOException;
import java.io.InputStream;

public class Stream extends InputStream {

	private InputStream is;
	private Long remaining;

	public Stream(InputStream is, Long size) {
		this.is = is;
		this.remaining = size;
	}

	@Override
	public int read() throws IOException {
		if (0 == remaining)
			return -1;
		int res = is.read();
		remaining--;
		return res;
	}

	@Override
	public int read(byte[] buf) throws IOException {
		if (0 == remaining)
			return -1;
		int res = is.read(buf,0, Math.min(buf.length,remaining.intValue()));
		remaining = remaining - res;
		return res;
	}

	@Override
	public int read(byte[] buf, int offset, int len) throws IOException {
		if (0 == remaining) {
			return -1;
		}
		int res = is.read(buf, offset,Math.min(len,Math.min(buf.length-offset,remaining.intValue())));
		remaining = remaining - res;
		return res;
	}

	@Override
	public void close() throws IOException {
		is.close();
	}
}
