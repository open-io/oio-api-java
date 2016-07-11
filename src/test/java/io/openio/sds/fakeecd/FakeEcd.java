package io.openio.sds.fakeecd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class FakeEcd extends Server {

	private byte[] lastData;

	private static String[] headers = { "x-oio-req-id",
	        "X-oio-chunk-meta-container-id",
	        "X-oio-chunk-meta-content-path",
	        "X-oio-chunk-meta-content-version",
	        "X-oio-chunk-meta-content-id",
	        "X-oio-chunk-meta-content-storage-policy",
	        "X-oio-chunk-meta-content-chunk-method",
	        "X-oio-chunk-meta-content-mime-type",
	        "X-oio-chunk-meta-chunk-0",
	        "X-oio-chunk-meta-chunk-1",
	        "X-oio-chunk-meta-chunk-2",
	        "X-oio-chunk-meta-chunk-3",
	        "X-oio-chunk-meta-chunk-4",
	        "X-oio-chunk-meta-chunk-5",
	        "X-oio-chunk-meta-chunk-6",
	        "X-oio-chunk-meta-chunk-7",
	        "X-oio-chunk-meta-chunk-8",
	        "X-oio-chunk-meta-chunks-nb",
	        "X-oio-chunk-meta-chunk-pos" };

	public FakeEcd(int port) {
		super(port);
		this.setHandler(new FakeEcdHandler());
	}

	public byte[] getLastData() {
		return lastData;
	}

	private class FakeEcdHandler extends AbstractHandler {

		@Override
		public void handle(String target, Request baseRequest,
		        HttpServletRequest request, HttpServletResponse response)
		                throws IOException, ServletException {
			baseRequest.setHandled(true);
			// dump informations
			System.out.println(request.getMethod() + " " + target + " "
			        + request.getProtocol());
			for (String hname : headers) {
				System.out.println(hname + ":" + baseRequest.getHeader(hname));
			}
			System.out.println();

			if (!"/".equals(target)) {
				System.out.println("Bad target " + target + " expected /");
				response.setStatus(400);
				return;
			}

			if (!"PUT".equals(request.getMethod())) {
				System.out.println(
				        "Bad method " + request.getMethod() + " expected PUT");
				response.setStatus(400);
				return;
			}

			// all right
			response.setStatus(200);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int nbRead = 0;
			byte[] buf = new byte[32 * 8192];
			InputStream in = request.getInputStream();
			while (-1 < (nbRead = in.read(buf)))
				out.write(buf, 0, nbRead);
			lastData = out.toByteArray();
		}

	}

}
