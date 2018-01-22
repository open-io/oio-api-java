package io.openio.sds.storage;

import java.io.File;
import java.io.InputStream;

import io.openio.sds.RequestContext;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.Range;

/**
 * Client for erasure coding service. Useful since we don't have EC directly in
 * java.
 */
public interface StorageClient {

    public ObjectInfo uploadChunks(ObjectInfo oinf, InputStream data);

    public ObjectInfo uploadChunks(ObjectInfo oinf, InputStream data, RequestContext reqCtx);

    public ObjectInfo uploadChunks(ObjectInfo oinf, File data);

    public ObjectInfo uploadChunks(ObjectInfo oinf, File data, RequestContext reqCtx);

    public ObjectInfo uploadChunks(ObjectInfo oinf, byte[] data);

    public ObjectInfo uploadChunks(ObjectInfo oinf, byte[] data, RequestContext reqCtx);

    public InputStream downloadObject(ObjectInfo oinf);

    public InputStream downloadObject(ObjectInfo oinf, Range range);

    public InputStream downloadObject(ObjectInfo oinf, RequestContext reqCtx);

    public InputStream downloadObject(ObjectInfo oinf, Range range, RequestContext reqCtx);

}
