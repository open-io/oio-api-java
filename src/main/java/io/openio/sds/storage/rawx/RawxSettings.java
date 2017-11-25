package io.openio.sds.storage.rawx;

import io.openio.sds.http.OioHttpSettings;

/**
 * @author Christopher Dedeurwaerder
 */
public class RawxSettings {

	private OioHttpSettings http = new OioHttpSettings();

	private boolean quorumWrite = false;

	public RawxSettings() {
	}

	public OioHttpSettings http() {
		return http;
	}

	public RawxSettings http(OioHttpSettings http) {
		this.http = http;
		return this;
	}

	public RawxSettings quorumWrite(boolean quorum) {
		this.quorumWrite = quorum;
		return this;
	}

	public boolean quorumWrite() {
		return quorumWrite;
	}
}