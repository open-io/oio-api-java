package io.openio.sds;

import static io.openio.sds.common.Check.checkArgument;

import java.io.File;
import java.io.InputStream;

import io.openio.sds.http.OioHttp;
import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.ObjectList;
import io.openio.sds.models.OioUrl;
import io.openio.sds.settings.Settings;

public class DefaultClient implements Client {

    private final ProxyClient proxy;
    private final RawxClient rawx;

    DefaultClient(OioHttp http, Settings settings) {
        this.proxy = new ProxyClient(http, settings.proxy());
        this.rawx = new RawxClient(http, settings.rawx());
    }

    public ProxyClient proxy() {
        return proxy;
    }

    @Override
    public ContainerInfo createContainer(OioUrl url) {
        checkArgument(null != url, "url cannot be null");
        return proxy.createContainer(url);
    }

    @Override
    public ContainerInfo getContainerInfo(OioUrl url) {
        checkArgument(null != url, "url cannot be null");
        return proxy.getContainerInfo(url);
    }

    @Override
    public ObjectList listContainer(OioUrl url, ListOptions listOptions) {
        checkArgument(null != url, "url cannot be null");
        checkArgument(null != listOptions, "listOptions cannot be null");
        return proxy.listContainer(url, listOptions);
    }

    @Override
    public void deleteContainer(OioUrl url) {
        checkArgument(null != url, "url cannot be null");
        proxy.deleteContainer(url);
    }

    @Override
    public ObjectInfo putObject(OioUrl url, long size, File data) {
        checkArgument(null != url, "url cannot be null");
        checkArgument(null != url.object(), "url object cannot be null");
        ObjectInfo oinf = proxy.getBeans(url, size);
        rawx.uploadChunks(oinf, data);
        proxy.putObject(oinf);
        return oinf;
    }

    @Override
    public ObjectInfo putObject(OioUrl url, long size, InputStream data) {
        checkArgument(null != url, "url cannot be null");
        checkArgument(null != url.object(), "url object cannot be null");
        ObjectInfo oinf = proxy.getBeans(url, size);
        rawx.uploadChunks(oinf, data);
        proxy.putObject(oinf);
        return oinf;
    }

    @Override
    public ObjectInfo getObjectInfo(OioUrl url) {
        checkArgument(null != url, "url cannot be null");
        checkArgument(null != url.object(), "url object cannot be null");
        return proxy.getObjectInfo(url);
    }

    @Override
    public InputStream downloadObject(ObjectInfo oinf) {
        checkArgument(null != oinf, "ObjectInfo cannot be null");
        return rawx.downloadObject(oinf);
    }

    @Override
    public void deleteObject(OioUrl url) {
        checkArgument(null != url, "url cannot be null");
        checkArgument(null != url.object(), "url object cannot be null");
        proxy.deleteObject(url);
    }
}
