package io.openio.sds.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ChunkedStream extends InputStream {

	private InputStream chunked;
	private int currentRemaining = 0;
	private boolean first = true;

	public ChunkedStream(InputStream chunked) {
		this.chunked = chunked;
	}

	@Override
	public int read() throws IOException {
		if (-1 == currentRemaining)
			return -1;
		if (0 == currentRemaining)
			readSize();
		int res = chunked.read();
		currentRemaining--;
		return res;
	}

	@Override
	public int read(byte[] buf) throws IOException {
		if (-1 == currentRemaining)
			return -1;
		if (0 == currentRemaining)
			readSize();
		int res = chunked.read(buf, 0, Math.min(buf.length, currentRemaining));
		currentRemaining = currentRemaining - res;
		return res;
	}

	@Override
	public int read(byte[] buf, int offset, int len) throws IOException {
		if (0 == currentRemaining)
			readSize();
		if (-1 == currentRemaining)
			return -1;
		int res = chunked.read(buf, offset,
		        Math.min(len, Math.min(buf.length - offset, currentRemaining)));
		currentRemaining = currentRemaining - res;
		return res;
	}

	@Override
	public void close() throws IOException {
		chunked.close();
	}

	private void readSize() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte b;
		if(!first)
			readCRLF();
		first=false;
		while ('\r' != (b = (byte) chunked.read())) { // 'til \r
			out.write(b);
		}
		chunked.read(); // read \n
		String line = out.toString("utf-8");
		currentRemaining = Integer.parseInt(out.toString("utf-8"),
		        16);
		if (0 == currentRemaining) { // EOF
			readCRLF();
			currentRemaining = -1;
		}
	}

	private void readCRLF() throws IOException {
		chunked.read();
		chunked.read();
	}

}
