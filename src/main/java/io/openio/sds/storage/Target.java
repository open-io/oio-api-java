package io.openio.sds.storage;

import java.util.List;

import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.Range;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class Target {

	private List<ChunkInfo> ci;
	private Range range;
	
	public Target() {
	}

	public List<ChunkInfo> getChunk() {
		return ci;
	}

	public Target setChunk(List<ChunkInfo> ci) {
		this.ci = ci;
		return this;
	}

	public Range getRange() {
		return range;
	}

	public Target setRange(Range range) {
		this.range = range;
		return this;
	}
	
	
	
}
