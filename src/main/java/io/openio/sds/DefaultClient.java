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
public class DefaultClient implements AdvancedClient {

    private final ProxyClient proxy;
    protected final RawxClient rawx;
    private final EcdClient ecd;

    DefaultClient(ProxyClient proxy, RawxClient rawx) {
        this(proxy, rawx, null);
    }

    DefaultClient(ProxyClient proxy, RawxClient rawx, EcdClient ecd) {
        this.proxy = proxy;
        this.rawx = rawx;
        this.ecd = ecd;
    }

    public ProxyClient proxy() {
        return proxy;
    }

    @Override
    public NamespaceInfo getNamespaceInfo() {
        return this.getNamespaceInfo(new RequestContext());
    }

    @Override
    public NamespaceInfo getNamespaceInfo(RequestContext reqCtx) {
        reqCtx.startTiming();
        return proxy.getNamespaceInfo(reqCtx);
    }

    @Override
    public ContainerInfo createContainer(OioUrl url) {
        return this.createContainer(url, new RequestContext());
    }

    @Override
    public ContainerInfo createContainer(OioUrl url, RequestContext reqCtx) throws OioException {
        checkArgument(url != null, "url cannot be null");
        reqCtx.startTiming();
        return proxy.createContainer(url, reqCtx);
    }

    @Override
    public ContainerInfo getContainerInfo(OioUrl url) {
        return this.getContainerInfo(url, new RequestContext());
    }

    @Override
    public ContainerInfo getContainerInfo(OioUrl url, RequestContext reqCtx) throws OioException {
        checkArgument(url != null, "url cannot be null");
        reqCtx.startTiming();
        return proxy.getContainerInfo(url, reqCtx);
    }

    @Override
    public ObjectList listContainer(OioUrl url, ListOptions listOptions) {
        return this.listObjects(url, listOptions, new RequestContext());
    }

    @Override
    public ObjectList listObjects(OioUrl url, final ListOptions listOptions) throws OioException {
        return this.listObjects(url, listOptions, new RequestContext());
    }

    @Override
    public ObjectList listObjects(OioUrl url, ListOptions listOptions, RequestContext reqCtx)
            throws OioException {
        checkArgument(url != null, "url cannot be null");
        checkArgument(listOptions != null, "listOptions cannot be null");
        reqCtx.startTiming();
        return proxy.listObjects(url, listOptions, reqCtx);
    }

    @Override
    public void deleteContainer(OioUrl url) {
        this.deleteContainer(url, new RequestContext());
    }

    @Override
    public void deleteContainer(OioUrl url, RequestContext reqCtx) {
        checkArgument(url != null, "url cannot be null");
        reqCtx.startTiming();
        proxy.deleteContainer(url, reqCtx);
    }

    @Override
    public ObjectInfo putObject(OioUrl url, Long size, File data) {
        return putObject(url, size, data, null, null);
    }

    @Override
    public ObjectInfo putObject(OioUrl url, Long size, File data, Map<String, String> properties)
            throws OioException {
        return putObject(url, size, data, null, properties);
    }

    @Override
    public ObjectInfo putObject(OioUrl url, Long size, File data, Long version) {
        return putObject(url, size, data, version, null);
    }

    @Override
    public ObjectInfo putObject(OioUrl url, Long size, File data, Long version,
            Map<String, String> properties) throws OioException {
        checkArgument(null != url, "url cannot be null");
        return putObject(url, size, data, version, properties, new RequestContext());
    }

    @Override
    public ObjectInfo putObject(OioUrl url, Long size, File data, Long version,
            Map<String, String> properties, RequestContext reqCtx) throws OioException {
        checkArgument(url != null, "url cannot be null");
        checkArgument(url.object() != null, "object part of URL cannot be null");
        reqCtx.startTiming();
        ObjectInfo oinf = proxy.preparePutObject(url, size, reqCtx);
        oinf.properties(properties);
        try {
            if (oinf.isEC())
                ecd.uploadChunks(oinf, data, reqCtx.requestId());
            else
                rawx.uploadChunks(oinf, data, reqCtx.requestId());
            proxy.putObject(oinf, version, reqCtx);
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
    public ObjectInfo putObject(OioUrl url, Long size, InputStream data, Long version) {
        return putObject(url, size, data, version, null);
    }

    @Override
    public ObjectInfo putObject(OioUrl url, Long size, InputStream data, Long version,
            Map<String, String> properties) throws OioException {
        checkArgument(url != null, "url cannot be null");
        return putObject(url, size, data, version, properties, new RequestContext());
    }

    @Override
    public ObjectInfo putObject(OioUrl url, Long size, InputStream data, Long version,
            Map<String, String> properties, RequestContext reqCtx) throws OioException {
        checkArgument(url != null, "url cannot be null");
        checkArgument(url.object() != null, "object part of URL cannot be null");
        reqCtx.startTiming();
        ObjectInfo oinf = proxy.preparePutObject(url, size, reqCtx);
        oinf.properties(properties);
        try {
            if (oinf.isEC())
                ecd.uploadChunks(oinf, data, reqCtx.requestId());
            else
                rawx.uploadChunks(oinf, data, reqCtx.requestId());
            proxy.putObject(oinf, version, reqCtx);
        } catch (OioException oioe) {
            // TODO improve by knowing which chunk is uploaded
            rawx.deleteChunks(oinf.chunks());
            throw oioe;
        }
        return oinf;
    }

    @Override
    public ObjectInfo getObjectInfo(OioUrl url) {
        return getObjectInfo(url, true);
    }

    @Override
    public ObjectInfo getObjectInfo(OioUrl url, boolean loadProperties) {
        return getObjectInfo(url, null, loadProperties);
    }

    @Override
    public ObjectInfo getObjectInfo(OioUrl url, Long version) {
        return getObjectInfo(url, version, true);
    }

    @Override
    public ObjectInfo getObjectInfo(OioUrl url, Long version, boolean loadProperties) {
        return this.getObjectInfo(url, version, loadProperties, new RequestContext());
    }

    @Override
    public ObjectInfo getObjectInfo(OioUrl url, Long version, boolean loadProperties,
            RequestContext reqCtx) throws OioException {
        checkArgument(url != null, "url cannot be null");
        checkArgument(url.object() != null, "url object cannot be null");
        reqCtx.startTiming();
        return proxy.getObjectInfo(url, version, reqCtx, loadProperties);
    }

    @Override
    public InputStream downloadObject(ObjectInfo oinf) {
        checkArgument(oinf != null, "ObjectInfo cannot be null");
        RequestContext reqCtx = oinf.requestContext().resetDeadline();
        reqCtx.startTiming();
        return oinf.isEC() ? ecd.downloadObject(oinf, reqCtx.requestId()) : rawx.downloadObject(
                oinf, reqCtx.requestId());
    }

    @Override
    public InputStream downloadObject(ObjectInfo oinf, Range range) {
        RequestContext reqCtx = oinf.requestContext().resetDeadline();
        return this.downloadObject(oinf, range,
                new RequestContext().withRequestId(reqCtx.requestId()));
    }

    @Override
    public InputStream downloadObject(ObjectInfo oinf, Range range, RequestContext reqCtx) {
        checkArgument(oinf != null, "ObjectInfo cannot be null");
        reqCtx.startTiming();
        return oinf.isEC() ? ecd.downloadObject(oinf, range, reqCtx.requestId()) : rawx
                .downloadObject(oinf, range, reqCtx.requestId());
    }

    @Override
    public void deleteObject(OioUrl url) {
        this.deleteObject(url, null, new RequestContext());
    }

    @Override
    public void deleteObject(OioUrl url, RequestContext reqCtx) throws OioException {
        this.deleteObject(url, null, reqCtx);
    }

    @Override
    public void deleteObject(OioUrl url, Long version) {
        this.deleteObject(url, version, new RequestContext());
    }

    @Override
    public void deleteObject(OioUrl url, Long version, RequestContext reqCtx) throws OioException {
        checkArgument(url != null, "url cannot be null");
        checkArgument(url.object() != null, "url object cannot be null");
        reqCtx.startTiming();
        proxy.deleteObject(url, version, reqCtx);
    }

    @Override
    public void setContainerProperties(OioUrl url, Map<String, String> props) throws OioException {
        this.setContainerProperties(url, props, new RequestContext());
    }

    @Override
    public void setContainerProperties(OioUrl url, Map<String, String> props, RequestContext reqCtx)
            throws OioException {
        reqCtx.startTiming();
        proxy.setContainerProperties(url, props, reqCtx);
    }

    @Override
    public Map<String, String> getContainerProperties(OioUrl url) throws OioException {
        return this.getContainerProperties(url, new RequestContext());
    }

    @Override
    public Map<String, String> getContainerProperties(OioUrl url, RequestContext reqCtx)
            throws OioException {
        reqCtx.startTiming();
        return proxy.getContainerProperties(url, reqCtx);
    }

    @Override
    public void deleteContainerProperties(OioUrl url, String... keys) throws OioException {
        this.deleteContainerProperties(url, new RequestContext(), keys);
    }

    @Override
    public void deleteContainerProperties(OioUrl url, RequestContext reqCtx, String... keys)
            throws OioException {
        reqCtx.startTiming();
        proxy.deleteContainerProperties(reqCtx, url, keys);
    }

    @Override
    public void deleteContainerProperties(OioUrl url, List<String> keys) throws OioException {
        this.deleteContainerProperties(url, keys, new RequestContext());
    }

    @Override
    public void deleteContainerProperties(OioUrl url, List<String> keys, RequestContext reqCtx)
            throws OioException {
        reqCtx.startTiming();
        proxy.deleteContainerProperties(url, keys, reqCtx);
    }

    @Override
    public void setObjectProperties(OioUrl url, Map<String, String> props) throws OioException {
        this.setObjectProperties(url, props, new RequestContext());
    }

    @Override
    public void setObjectProperties(OioUrl url, Map<String, String> props, RequestContext reqCtx)
            throws OioException {
        reqCtx.startTiming();
        proxy.setObjectProperties(url, props, reqCtx);
    }

    @Override
    public Map<String, String> getObjectProperties(OioUrl url) throws OioException {
        return this.getObjectProperties(url, new RequestContext());
    }

    @Override
    public Map<String, String> getObjectProperties(OioUrl url, RequestContext reqCtx)
            throws OioException {
        reqCtx.startTiming();
        return proxy.getObjectProperties(url, reqCtx);
    }

    @Override
    public void deleteObjectProperties(OioUrl url, String... keys) throws OioException {
        this.deleteObjectProperties(url, new RequestContext(), keys);
    }

    @Override
    public void deleteObjectProperties(OioUrl url, List<String> keys) throws OioException {
        this.deleteObjectProperties(url, keys, new RequestContext());
    }

    @Override
    public void deleteObjectProperties(OioUrl url, RequestContext reqCtx, String... keys)
            throws OioException {
        reqCtx.startTiming();
        proxy.deleteObjectProperties(reqCtx, url, keys);
    }

    @Override
    public void deleteObjectProperties(OioUrl url, List<String> keys, RequestContext reqCtx)
            throws OioException {
        reqCtx.startTiming();
        proxy.deleteObjectProperties(url, keys, reqCtx);
    }
}
