package io.openio.sds;

import static io.openio.sds.common.Check.checkArgument;
import static io.openio.sds.common.IdGen.requestId;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttp;
import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.NamespaceInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.ObjectList;
import io.openio.sds.models.OioUrl;
import io.openio.sds.models.Range;
import io.openio.sds.proxy.ProxyClient;
import io.openio.sds.storage.ecd.EcdClient;
import io.openio.sds.storage.rawx.RawxClient;

/**
 * Basis implementation of {@link Client} interface based on {@link OioHttp}
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class DefaultClient implements Client {

	private final ProxyClient proxy;
	protected final RawxClient rawx;
	private final EcdClient ecd;

	DefaultClient(ProxyClient proxy, 
			RawxClient rawx) {
		this(proxy, rawx, null);
	}
	
	DefaultClient(ProxyClient proxy, 
			RawxClient rawx,
			EcdClient ecd) {
		this.proxy = proxy;
		this.rawx = rawx;
		this.ecd = ecd;
	}

	public ProxyClient proxy() {
		return proxy;
	}

	@Override
	public NamespaceInfo getNamespaceInfo() {
		return proxy.getNamespaceInfo();
	}

	@Override
	public ContainerInfo createContainer(OioUrl url) {
		checkArgument(null != url, "url cannot be null");
		return proxy.createContainer(url, requestId());
	}

	@Override
	public ContainerInfo getContainerInfo(OioUrl url) {
		checkArgument(null != url, "url cannot be null");
		return proxy.getContainerInfo(url, requestId());
	}

	@Override
	public ObjectList listContainer(OioUrl url, ListOptions listOptions) {
		checkArgument(null != url, "url cannot be null");
		checkArgument(null != listOptions, "listOptions cannot be null");
		return proxy.listContainer(url, listOptions, requestId());
	}

	@Override
	public void deleteContainer(OioUrl url) {
		checkArgument(null != url, "url cannot be null");
		proxy.deleteContainer(url, requestId());
	}

	@Override
	public ObjectInfo putObject(OioUrl url, Long size, File data) {
		return putObject(url, size, data, null, null);
	}

	@Override
	public ObjectInfo putObject(OioUrl url, Long size, File data,
	        Map<String, String> properties) throws OioException {
		return putObject(url, size, data, null, properties);
	}

	@Override
	public ObjectInfo putObject(OioUrl url, Long size, File data,
	        Long version) {
		return putObject(url, size, data, version, null);
	}

	@Override
	public ObjectInfo putObject(OioUrl url, Long size, File data, Long version,
	        Map<String, String> properties) throws OioException {
		checkArgument(null != url, "url cannot be null");
		checkArgument(null != url.object(), "url object cannot be null");
		String reqId = requestId();
		ObjectInfo oinf = proxy.getBeans(url, size, reqId);
		oinf.properties(properties);
		try {
			if(oinf.isEC())
				ecd.uploadChunks(oinf, data, reqId);
			else
				rawx.uploadChunks(oinf, data, reqId);
			proxy.putObject(oinf, reqId, version);
		} catch (OioException e) {
			// TODO improve by knowing which chunk is uploaded
			rawx.deleteChunks(oinf.chunks());
		}
		return oinf;
	}

	@Override
	public ObjectInfo putObject(OioUrl url, Long size, InputStream data) {
		return putObject(url, size, data, null, null);
	}

	@Override
	public ObjectInfo putObject(OioUrl url, Long size, InputStream data,
	        Map<String, String> properties) throws OioException {
		return putObject(url, size, data, null, properties);
	}

	@Override
	public ObjectInfo putObject(OioUrl url, Long size, InputStream data,
	        Long version) {
		return putObject(url, size, data, version, null);
	}

	@Override
	public ObjectInfo putObject(OioUrl url, Long size, InputStream data,
	        Long version, Map<String, String> properties) throws OioException {
		checkArgument(null != url, "url cannot be null");
		checkArgument(null != url.object(), "url object cannot be null");
		String reqId = requestId();
		ObjectInfo oinf = proxy.getBeans(url, size, reqId);
		oinf.properties(properties);
		try {
			if(oinf.isEC())
				ecd.uploadChunks(oinf, data, reqId);
			else
				rawx.uploadChunks(oinf, data, reqId);
			proxy.putObject(oinf, reqId, version);
		} catch (OioException oioe) {
			// TODO improve by knowing which chunk is uploaded
			rawx.deleteChunks(oinf.chunks());
			throw oioe;
		}
		return oinf;
	}

	@Override
	public ObjectInfo getObjectInfo(OioUrl url) {
		return getObjectInfo(url, null);
	}

	@Override
	public ObjectInfo getObjectInfo(OioUrl url, Long version) {
		checkArgument(null != url, "url cannot be null");
		checkArgument(null != url.object(), "url object cannot be null");
		return proxy.getObjectInfo(url, version, requestId());
	}

	@Override
	public InputStream downloadObject(ObjectInfo oinf) {
		checkArgument(null != oinf, "ObjectInfo cannot be null");
		return oinf.isEC() 
				? ecd.downloadObject(oinf, requestId())
				: rawx.downloadObject(oinf, requestId());
	}
	
	@Override
	public InputStream downloadObject(ObjectInfo oinf, Range range) {
		checkArgument(null != oinf, "ObjectInfo cannot be null");
		return oinf.isEC() 
				? ecd.downloadObject(oinf, range, requestId())
				: rawx.downloadObject(oinf, range,  requestId());
	}

	@Override
	public void deleteObject(OioUrl url) {
		deleteObject(url, null);
	}

	@Override
	public void deleteObject(OioUrl url, Long version) {
		checkArgument(null != url, "url cannot be null");
		checkArgument(null != url.object(), "url object cannot be null");
		proxy.deleteObject(url, version, requestId());
	}

	@Override
	public void setContainerProperties(OioUrl url, Map<String, String> props)
	        throws OioException {
		proxy.setContainerProperties(url, props, requestId());
	}

	@Override
	public Map<String, String> getContainerProperties(OioUrl url)
	        throws OioException {
		return proxy.getContainerProperties(url, requestId());
	}

	@Override
	public void deleteContainerProperties(OioUrl url, String... keys)
	        throws OioException {
		proxy.deleteContainerProperties(requestId(), url, keys);
	}

	@Override
	public void deleteContainerProperties(OioUrl url, List<String> keys)
	        throws OioException {
		proxy.deleteContainerProperties(url, keys, requestId());
	}

	@Override
	public void setObjectProperties(OioUrl url, Map<String, String> props)
	        throws OioException {
		proxy.setObjectProperties(url, props, requestId());
	}

	@Override
	public Map<String, String> getObjectProperties(OioUrl url)
	        throws OioException {
		return proxy.getObjectProperties(url, requestId());
	}

	@Override
	public void deleteObjectProperties(OioUrl url, String... keys)
	        throws OioException {
		proxy.deleteObjectProperties(requestId(), url, keys);
	}

	@Override
	public void deleteObjectProperties(OioUrl url, List<String> keys)
	        throws OioException {
		proxy.deleteObjectProperties(url, keys, requestId());
	}

}
