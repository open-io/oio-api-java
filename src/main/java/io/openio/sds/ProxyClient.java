package io.openio.sds;

import static io.openio.sds.common.JsonUtils.gson;
import static io.openio.sds.common.OioConstants.*;
import static io.openio.sds.common.OioConstants.ACCOUNT_HEADER;
import static io.openio.sds.common.OioConstants.CONTAINER_SYS_NAME_HEADER;
import static io.openio.sds.common.OioConstants.CONTENT_META_CHUNK_METHOD_HEADER;
import static io.openio.sds.common.OioConstants.CONTENT_META_CTIME_HEADER;
import static io.openio.sds.common.OioConstants.CONTENT_META_HASH_HEADER;
import static io.openio.sds.common.OioConstants.CONTENT_META_HASH_METHOD_HEADER;
import static io.openio.sds.common.OioConstants.CONTENT_META_ID_HEADER;
import static io.openio.sds.common.OioConstants.CONTENT_META_LENGTH_HEADER;
import static io.openio.sds.common.OioConstants.CONTENT_META_MIME_TYPE_HEADER;
import static io.openio.sds.common.OioConstants.CONTENT_META_POLICY_HEADER;
import static io.openio.sds.common.OioConstants.CONTENT_META_VERSION_HEADER;
import static io.openio.sds.common.OioConstants.M2_CTIME_HEADER;
import static io.openio.sds.common.OioConstants.M2_INIT_HEADER;
import static io.openio.sds.common.OioConstants.M2_USAGE_HEADER;
import static io.openio.sds.common.OioConstants.M2_VERSION_HEADER;
import static io.openio.sds.common.OioConstants.NS_HEADER;
import static io.openio.sds.common.OioConstants.OIO_ACTION_MODE_HEADER;
import static io.openio.sds.common.OioConstants.OIO_CHARSET;
import static io.openio.sds.common.OioConstants.SCHEMA_VERSION_HEADER;
import static io.openio.sds.common.OioConstants.TYPE_HEADER;
import static io.openio.sds.common.OioConstants.USER_NAME_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_ADMIN_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_ALIASES_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_CHUNKS_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_CONTENTS_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_PROPERTIES_HEADER;
import static io.openio.sds.http.OioHttpHelper.longHeader;
import static java.lang.String.format;

import java.io.InputStreamReader;
import java.util.List;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import io.openio.sds.exceptions.BadRequestException;
import io.openio.sds.exceptions.ContainerExistException;
import io.openio.sds.exceptions.ContainerNotFoundException;
import io.openio.sds.exceptions.ObjectNotFoundException;
import io.openio.sds.exceptions.SdsException;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttpResponse;
import io.openio.sds.http.OioHttpResponseVerifier;
import io.openio.sds.models.BeansRequest;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.NamespaceInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.ObjectList;
import io.openio.sds.models.OioUrl;
import io.openio.sds.models.ServiceInfo;
import io.openio.sds.settings.ProxySettings;

public class ProxyClient {

   
    private OioHttp http;
    private ProxySettings settings;

    public ProxyClient(OioHttp http, ProxySettings settings) {
        this.http = http;
        this.settings = settings;
    }

    public NamespaceInfo getNamespaceInfo() {
        return http.get(format(NSINFO_FORMAT,
                settings.url(), settings.ns()))
                .verifier(STANDALONE_VERIFIER)
                .execute(NamespaceInfo.class);
    }

    public List<ServiceInfo> getServices(String type) {
        OioHttpResponse resp = http
                .get(format(GETSRV_FORMAT,
                        settings.url(), settings.ns(), type))
                .verifier(STANDALONE_VERIFIER)
                .execute();
        return serviceListAndClose(resp);
    }

    public void registerService(String type, ServiceInfo si) {
        http.get(format(REGISTER_FORMAT,
                settings.url(), settings.ns(), type))
                .body(gson().toJson(si))
                .verifier(STANDALONE_VERIFIER)
                .execute()
                .close();
    }

    public void linkService(OioUrl url, String type) {
        // TODO
    }

    /**
     * Creates a container using the specified {@code OioUrl}
     * 
     * @param url
     *            the url of the container to create
     * @return {@code ContainerInfo}
     */
    public ContainerInfo createContainer(OioUrl url) {
        OioHttpResponse resp = http.post(format(CREATE_CONTAINER_FORMAT,
                settings.url(), url.account(), url.container()))
                .header(OIO_ACTION_MODE_HEADER, "autocreate")
                .verifier(CONTAINER_VERIFIER)
                .execute()
                .close();
        if(201 == resp.code())
            throw new ContainerExistException("Container alreay present");

        return new ContainerInfo(url.container());
    }

    /**
     * Returns informations about the specified container
     * 
     * @param url
     * @param listener
     * @return the container informations
     */
    public ContainerInfo getContainerInfo(OioUrl url) {
        OioHttpResponse r = http.get(
                format(GET_CONTAINER_INFO_FORMAT,
                        settings.url(), settings.ns(), url.account(),
                        url.container()))
                .verifier(CONTAINER_VERIFIER)
                .execute();
        r.close();
        return new ContainerInfo(url.container())
                .account(r.header(ACCOUNT_HEADER))
                .ctime(longHeader(r, M2_CTIME_HEADER))
                .init(longHeader(r, M2_INIT_HEADER))
                .usage(longHeader(r, M2_USAGE_HEADER))
                .version(longHeader(r, M2_VERSION_HEADER))
                .id(r.header(CONTAINER_SYS_NAME_HEADER))
                .ns(r.header(NS_HEADER))
                .type(r.header(TYPE_HEADER))
                .user(r.header(USER_NAME_HEADER))
                .schemavers(r.header(SCHEMA_VERSION_HEADER))
                .versionMainAdmin(r.header(VERSION_MAIN_ADMIN_HEADER))
                .versionMainAliases(r.header(VERSION_MAIN_ALIASES_HEADER))
                .versionMainChunks(r.header(VERSION_MAIN_CHUNKS_HEADER))
                .versionMainContents(
                        r.header(VERSION_MAIN_CONTENTS_HEADER))
                .versionMainProperties(
                        r.header(VERSION_MAIN_PROPERTIES_HEADER));
    }

    public ObjectList listContainer(OioUrl url, ListOptions options) {
        return http.get(
                format(LIST_OBJECTS_FORMAT,
                        settings.url(), settings.ns(), url.account(),
                        url.container()))
                .query(MAX_PARAM, options.limit() > 0
                        ? String.valueOf(options.limit()) : null)
                .query(PREFIX_PARAM, options.prefix())
                .query(MARKER_PARAM, options.marker())
                .query(DELIMITER_PARAM, options.delimiter())
                .verifier(CONTAINER_VERIFIER)
                .execute(ObjectList.class);
    }

    public void deleteContainer(OioUrl url) {
        http.post(format(DELETE_CONTAINER_FORMAT,
                settings.url(), settings.ns(), url.account(),
                url.container()))
                .verifier(CONTAINER_VERIFIER)
                .execute()
                .close();
    }

    public ObjectInfo getBeans(OioUrl url, long size)
            throws JsonSyntaxException {
        OioHttpResponse resp = http.post(
                format(GET_BEANS_FORMAT,
                        settings.url(), settings.ns(), url.account(),
                        url.container(), url.object()))
                .body(gson().toJson(new BeansRequest().size(size)))
                .verifier(OBJECT_VERIFIER)
                .execute();
        return objectInfoAndClose(url, resp);
    }

    public ObjectInfo putObject(ObjectInfo oinf) {
        http.post(format(PUT_OBJECT_FORMAT,
                settings.url(), settings.ns(),
                oinf.url().account(),
                oinf.url().container(),
                oinf.url().object()))
                .header(CONTENT_META_LENGTH_HEADER,
                        String.valueOf(oinf.size()))
                .header(CONTENT_META_HASH_HEADER, oinf.hash())
                .body(gson().toJson(oinf.chunks()))
                .verifier(OBJECT_VERIFIER)
                .execute()
                .close();
        return oinf;
    }

    public ObjectInfo getObjectInfo(OioUrl url) {
        OioHttpResponse resp = http.get(
                format(GET_OBJECT_FORMAT,
                        settings.url(), settings.ns(), url.account(),
                        url.container(), url.object()))
                .verifier(OBJECT_VERIFIER)
                .execute();
        return objectInfoAndClose(url, resp);
    }

    public void deleteObject(OioUrl url) {
        http.post(
                format(DELETE_OBJECT_FORMAT,
                        settings.url(), settings.ns(), url.account(),
                        url.container(), url.object()))
                .verifier(OBJECT_VERIFIER)
                .execute()
                .close();
    }

    private ObjectInfo objectInfoAndClose(OioUrl url, OioHttpResponse resp) {
        try {
            return fillObjectInfo(url, resp)
                    .chunks(bodyChunk(resp));
        } finally {
            resp.close();
        }
    }

    private ObjectInfo fillObjectInfo(OioUrl url, OioHttpResponse r) {
        return new ObjectInfo()
                .url(url)
                .oid(r.header(CONTENT_META_ID_HEADER))
                .size(longHeader(r, CONTENT_META_LENGTH_HEADER))
                .ctime(longHeader(r, CONTENT_META_CTIME_HEADER))
                .chunkMethod(r.header(CONTENT_META_CHUNK_METHOD_HEADER))
                .policy(r.header(CONTENT_META_POLICY_HEADER))
                .version(longHeader(r, CONTENT_META_VERSION_HEADER))
                .hashMethod(r.header(CONTENT_META_HASH_METHOD_HEADER))
                .mtype(r.header(CONTENT_META_MIME_TYPE_HEADER));
    }

    private List<ChunkInfo> bodyChunk(OioHttpResponse resp)
            throws SdsException {
        try {
            return gson().fromJson(
                    new JsonReader(
                            new InputStreamReader(resp.body(), OIO_CHARSET)),
                    new TypeToken<List<ChunkInfo>>() {
                    }.getType());
        } catch (Exception e) {
            throw new SdsException("Body extraction error", e);
        }
    }

    private List<ServiceInfo> serviceListAndClose(OioHttpResponse resp) {
        try {
            return gson().fromJson(
                    new JsonReader(
                            new InputStreamReader(resp.body(), OIO_CHARSET)),
                    new TypeToken<List<ServiceInfo>>() {
                    }.getType());
        } catch (Exception e) {
            throw new SdsException("Body extraction error", e);
        } finally {
            resp.close();
        }
    }

    private static final OioHttpResponseVerifier CONTAINER_VERIFIER = new OioHttpResponseVerifier() {

        @Override
        public void verify(OioHttpResponse resp) throws SdsException {
            switch (resp.code()) {
            case 200:
            case 201:
            case 204:
                return;
            case 400:
                throw new BadRequestException(resp.msg());
            case 404:
                throw new ContainerNotFoundException(resp.msg());
            case 500:
                throw new SdsException(
                        format(INTERNAL_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            default:
                throw new SdsException(
                        format(UNMANAGED_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            }
        }
    };

    private static final OioHttpResponseVerifier OBJECT_VERIFIER = new OioHttpResponseVerifier() {

        @Override
        public void verify(OioHttpResponse resp) throws SdsException {
            switch (resp.code()) {
            case 200:
            case 201:
            case 204:
                return;
            case 400:
                throw new BadRequestException(resp.msg());
            case 404:
                throw new ObjectNotFoundException(resp.msg());
            case 500:
                throw new SdsException(
                        format(INTERNAL_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            default:
                throw new SdsException(
                        format(UNMANAGED_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            }
        }
    };

    private static final OioHttpResponseVerifier STANDALONE_VERIFIER = new OioHttpResponseVerifier() {

        @Override
        public void verify(OioHttpResponse resp) throws SdsException {
            switch (resp.code()) {
            case 200:
            case 201:
            case 204:
                return;
            case 400:
                throw new BadRequestException(resp.msg());
            case 404:
                throw new SdsException(resp.msg());
            case 500:
                throw new SdsException(
                        format(INTERNAL_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            default:
                throw new SdsException(
                        format(UNMANAGED_ERROR_FORMAT, resp.code(),
                                resp.msg()));
            }
        }
    };
}
