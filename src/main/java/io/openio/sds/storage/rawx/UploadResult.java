package io.openio.sds.storage.rawx;

import io.openio.sds.exceptions.OioException;
import io.openio.sds.models.ChunkInfo;

public class UploadResult {
	ChunkInfo chunkInfo;
	OioException exception;

	public UploadResult(ChunkInfo ci) {
		chunkInfo = ci;
	}

	public ChunkInfo chunkInfo() {
		return chunkInfo;
	}

	public void exception(OioException e) {
		exception = e;
	}

	public OioException exception() {
		return exception;
	}
}
