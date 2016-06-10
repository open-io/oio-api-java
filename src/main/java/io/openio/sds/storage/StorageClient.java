package io.openio.sds.storage;

import java.io.File;
import java.io.InputStream;

import io.openio.sds.models.ObjectInfo;

/**
 * Client for erasure coding service. Useful since we don't have EC diretly in
 * java.
 */
public interface StorageClient {

	public ObjectInfo uploadChunks(ObjectInfo oinf, InputStream data);

	public ObjectInfo uploadChunks(ObjectInfo oinf, InputStream data,
	        String reqId);

	public ObjectInfo uploadChunks(ObjectInfo oinf, File data);

	public ObjectInfo uploadChunks(ObjectInfo oinf, File data, String reqId);

	public ObjectInfo uploadChunks(ObjectInfo oinf, byte[] data);

	public ObjectInfo uploadChunks(ObjectInfo oinf, byte[] data, String reqId);

	public InputStream downloadObject(ObjectInfo oinf);

	public InputStream downloadObject(ObjectInfo oinf, String reqId);

}
