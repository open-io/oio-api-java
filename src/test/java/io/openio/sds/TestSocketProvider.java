package io.openio.sds;

import io.openio.sds.common.AbstractSocketProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSocketProvider extends AbstractSocketProvider {

	private final List<ByteArrayOutputStream> outputs;
	private List<ByteArrayInputStream> inputs;

	public TestSocketProvider(List<ByteArrayInputStream> inputs) {
		this.inputs = inputs;
		this.outputs = new ArrayList<ByteArrayOutputStream>();
	}

	private synchronized InputStream nextInput() {
		if (!inputs.isEmpty()) {
			return inputs.remove(0);
		}
		return null;
	}

	private void addOutput(ByteArrayOutputStream os) {
		outputs.add(os);
	}

	public List<ByteArrayOutputStream> outputs() {
		return outputs;
	}

	@Override
	public Socket getSocket(String host, int port) {
		return null;
	}

	@Override
	public Socket getSocket(InetSocketAddress addr) {
		Socket sock = mock(Socket.class);
		when(sock.getLocalAddress()).thenReturn(addr.getAddress());
		when(sock.getLocalPort()).thenReturn(addr.getPort());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		addOutput(os);
		try {
			when(sock.getOutputStream()).thenReturn(os);
		} catch (IOException e) {
			e.printStackTrace();
		}
		InputStream is = nextInput();
		if (null != is) {
			try {
				when(sock.getInputStream()).thenReturn(is);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			try {
				when(sock.getInputStream()).thenThrow(IOException.class);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return sock;
	}

	@Override
	public boolean reusableSocket() {
		return false;
	}
}
