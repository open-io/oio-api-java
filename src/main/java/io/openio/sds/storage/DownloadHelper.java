package io.openio.sds.storage;

import static io.openio.sds.models.Range.between;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.Range;

public class DownloadHelper {

	public static List<Target> loadTargets(ObjectInfo oinf, Range range) {

		LinkedList<Target> targets = new LinkedList<Target>();

		if (null == range) {
			for (int i = 0; i < oinf.sortedChunks().size(); i++) {
				targets.addFirst(new Target()
				        .setChunk(oinf.sortedChunks().get(i)));
			}
		} else {
			ObjectOffset begin = findOffset(oinf, range.from());
			ObjectOffset end = findOffset(oinf, range.to());
			if (end.pos() == begin.pos()) {
				targets.addFirst(new Target()
				        .setChunk(oinf.sortedChunks().get(begin.pos()))
				        .setRange(between(begin.offset(), end.offset())));
			} else {
				targets.addFirst(new Target()
				        .setChunk(oinf.sortedChunks().get(begin.pos()))
				        .setRange(between(begin.offset(),
				                oinf.chunksize(begin.pos()).intValue()
				                        - 1)));
				for (int extrapos = begin.pos() + 1; extrapos < end
				        .pos(); extrapos++) {
					targets.addFirst(new Target()
					        .setChunk(oinf.sortedChunks().get(extrapos))
					        .setRange(between(0,
					                oinf.chunksize(extrapos).intValue())));
				}

				targets.addFirst(new Target()
				        .setChunk(oinf.sortedChunks().get(end.pos()))
				        .setRange(between(0, end.offset())));
			}
		}
		Collections.reverse(targets);
		return targets;
	}

	private static ObjectOffset findOffset(ObjectInfo oinf, int offset) {
		if (-1 == offset)
			return new ObjectOffset()
			        .pos(oinf.nbchunks() - 1)
			        .offset(oinf.chunksize(oinf.nbchunks() - 1).intValue() - 1);
		for (int pos = 0; pos < oinf.nbchunks(); pos++) {
			long size = oinf.chunksize(pos);
			if (offset <= size)
				return new ObjectOffset().pos(pos).offset(offset);
			offset -= size;
		}
		throw new IllegalArgumentException("Range begin out of content size");
	}
}
