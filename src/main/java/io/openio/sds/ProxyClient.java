package io.openio.sds;

import static io.openio.sds.common.Check.checkArgument;
import static io.openio.sds.common.JsonUtils.gson;
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
import static io.openio.sds.common.OioConstants.CREATE_CONTAINER_FORMAT;
import static io.openio.sds.common.OioConstants.CS_GETSRV_FORMAT;
import static io.openio.sds.common.OioConstants.CS_NSINFO_FORMAT;
import static io.openio.sds.common.OioConstants.CS_REGISTER_FORMAT;
import static io.openio.sds.common.OioConstants.DELETE_CONTAINER_FORMAT;
import static io.openio.sds.common.OioConstants.DELETE_OBJECT_FORMAT;
import static io.openio.sds.common.OioConstants.DELIMITER_PARAM;
import static io.openio.sds.common.OioConstants.DIR_LINK_SRV_FORMAT;
import static io.openio.sds.common.OioConstants.DIR_LIST_SRV_FORMAT;
import static io.openio.sds.common.OioConstants.DIR_REF_CREATE_FORMAT;
import static io.openio.sds.common.OioConstants.DIR_REF_DELETE_FORMAT;
import static io.openio.sds.common.OioConstants.DIR_REF_SHOW_FORMAT;
import static io.openio.sds.common.OioConstants.GET_BEANS_FORMAT;
import static io.openio.sds.common.OioConstants.GET_CONTAINER_INFO_FORMAT;
import static io.openio.sds.common.OioConstants.GET_OBJECT_FORMAT;
import static io.openio.sds.common.OioConstants.INTERNAL_ERROR_FORMAT;
import static io.openio.sds.common.OioConstants.LIST_OBJECTS_FORMAT;
import static io.openio.sds.common.OioConstants.M2_CTIME_HEADER;
import static io.openio.sds.common.OioConstants.M2_INIT_HEADER;
import static io.openio.sds.common.OioConstants.M2_USAGE_HEADER;
import static io.openio.sds.common.OioConstants.M2_VERSION_HEADER;
import static io.openio.sds.common.OioConstants.MARKER_PARAM;
import static io.openio.sds.common.OioConstants.MAX_PARAM;
import static io.openio.sds.common.OioConstants.NS_HEADER;
import static io.openio.sds.common.OioConstants.OIO_ACTION_MODE_HEADER;
import static io.openio.sds.common.OioConstants.OIO_CHARSET;
import static io.openio.sds.common.OioConstants.PREFIX_PARAM;
import static io.openio.sds.common.OioConstants.PUT_OBJECT_FORMAT;
import static io.openio.sds.common.OioConstants.SCHEMA_VERSION_HEADER;
import static io.openio.sds.common.OioConstants.TYPE_HEADER;
import static io.openio.sds.common.OioConstants.UNMANAGED_ERROR_FORMAT;
import static io.openio.sds.common.OioConstants.USER_NAME_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_ADMIN_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_ALIASES_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_CHUNKS_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_CONTENTS_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_PROPERTIES_HEADER;
import static io.openio.sds.common.Strings.nullOrEmpty;
import static io.openio.sds.http.OioHttpHelper.longHeader;
import static java.lang.String.format;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import io.openio.sds.exceptions.BadRequestException;
import io.openio.sds.exceptions.ContainerExistException;
import io.openio.sds.exceptions.ContainerNotFoundException;
import io.openio.sds.exceptions.ObjectNotFoundException;
import io.openio.sds.exceptions.ReferenceAlreadyExistException;
import io.openio.sds.exceptions.ReferenceNotFoundException;
import io.openio.sds.exceptions.SdsException;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttpResponse;
import io.openio.sds.http.OioHttpResponseVerifier;
import io.openio.sds.models.BeansRequest;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.LinkedServiceInfo;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.NamespaceInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.ObjectList;
import io.openio.sds.models.OioUrl;
import io.openio.sds.models.ReferenceInfo;
import io.openio.sds.models.ServiceInfo;
import io.openio.sds.settings.ProxySettings;

/**
 * Simple OpenIO proxyd http client based on API reference available at
 * https://github.com/open-io/oio-sds/wiki/OpenIO-Proxyd-API-Reference
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ProxyClient {

    private OioHttp http;
    private ProxySettings settings;

    public ProxyClient(OioHttp http, ProxySettings settings) {
        this.http = http;
        this.settings = settings;
    }

    /* -- CS -- */

    /**
     * Retrieves technical informations relative to the proxyd served namespace
     * 
     * @return the matching {@code NamespaceInfo}
     */
    public NamespaceInfo getNamespaceInfo() {
        return http.get(format(CS_NSINFO_FORMAT,
                settings.url(), settings.ns()))
                .verifier(STANDALONE_VERIFIER)
                .execute(NamespaceInfo.class);
    }

    public List<ServiceInfo> getServices(String type) {
        OioHttpResponse resp = http
                .get(format(CS_GETSRV_FORMAT,
                        settings.url(), settings.ns(), type))
                .verifier(STANDALONE_VERIFIER)
                .execute();
        return serviceInfoListAndClose(resp);
    }

    public void registerService(String type, ServiceInfo si) {
        http.get(format(CS_REGISTER_FORMAT,
                settings.url(), settings.ns(), type))
                .body(gson().toJson(si))
                .verifier(STANDALONE_VERIFIER)
                .execute()
                .close();
    }

    /* -- DIRECTORY -- */

    /**
     * Creates a reference in Oio directory.
     * 
     * @param url
     *            the url of the reference
     * 
     */
    public void createReference(OioUrl url) {
        checkArgument(null != url, "Missing url");
        http.post(format(DIR_REF_CREATE_FORMAT,
                settings.url(), settings.ns(),
                url.account(), url.container()))
                .verifier(REFERENCE_VERIFIER)
                .execute()
                .close();
    }

    /**
     * Retrieves informations about the specified reference
     * 
     * @param url
     *            the url of the reference to look for.
     * @throws SdsException
     *             if any error occurs during request execution
     */
    public ReferenceInfo showReference(OioUrl url)
            throws SdsException {
        checkArgument(null != url);
        return http.get(format(DIR_REF_SHOW_FORMAT,
                settings.url(), settings.ns(),
                url.account(), url.container()))
                .verifier(REFERENCE_VERIFIER)
                .execute(ReferenceInfo.class);
    }

    /**
     * Deletes a reference from Oio directory. Reference should not be linked to
     * a any service to be dropped
     * 
     * @param url
     *            the url of the reference
     * @throws SdsException
     *             if any error occurs during request execution
     * 
     */
    public void deleteReference(OioUrl url) {
        checkArgument(null != url, "Missing url");
        http.post(format(DIR_REF_DELETE_FORMAT,
                settings.url(), settings.ns(),
                url.account(), url.container()))
                .verifier(REFERENCE_VERIFIER)
                .execute()
                .close();
    }

    /**
     * Attachs a service of the specified type to a reference
     * 
     * @param url
     * @param type
     * @return
     */
    public List<LinkedServiceInfo> linkService(OioUrl url, String type) {
        checkArgument(!nullOrEmpty(type), "Missing type");
        OioHttpResponse resp = http.post(format(DIR_LINK_SRV_FORMAT,
                settings.url(), settings.ns(),
                url.account(), url.container(), type))
                .verifier(REFERENCE_VERIFIER)
                .execute();

        return listAndClose(resp);
    }

    /**
     * Retrieves informations about the specified reference
     * 
     * @param url
     *            the url of the reference to look for.
     * @throws SdsException
     *             if any error occurs during request execution
     */
    public List<LinkedServiceInfo> listServices(OioUrl url, String type)
            throws SdsException {
        checkArgument(null != url);
        checkArgument(!nullOrEmpty(type));
        return http.get(format(DIR_LIST_SRV_FORMAT,
                settings.url(), settings.ns(),
                url.account(), url.container(), type))
                .verifier(REFERENCE_VERIFIER)
                .execute(ReferenceInfo.class)
                .srv();
    }

    /**
     * Detachs services from the specified url
     * 
     * @param url
     */
    public void unlinkService(OioUrl url) {
        // TODO
    }

    /**
     * 
     * @param url
     */
    public void forceService(OioUrl url) {
        // TODO
    }

    /**
     * 
     * @return
     */
    public LinkedServiceInfo renewService(OioUrl url) {
        // TODO
        return null;
    }

    /**
     * Creates a container using the specified {@code OioUrl}
     * 
     * @param url
     *            the url of the container to create
     * @return {@code ContainerInfo}
     * @throws SdsException
     *             if any error occurs during request execution
     */
    public ContainerInfo createContainer(OioUrl url) throws SdsException {
        OioHttpResponse resp = http.post(format(CREATE_CONTAINER_FORMAT,
                settings.url(), url.account(), url.container()))
                .header(OIO_ACTION_MODE_HEADER, "autocreate")
                .verifier(CONTAINER_VERIFIER)
                .execute()
                .close();
        if (201 == resp.code())
            throw new ContainerExistException("Container alreay present");

        return new ContainerInfo(url.container());
    }

    /**
     * Returns informations about the specified container
     * 
     * @param url
     * @param listener
     * @return the container informations
     * @throws SdsException
     *             if any error occurs during request execution
     */
    public ContainerInfo getContainerInfo(OioUrl url) throws SdsException {
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

    /**
     * Lists all object available inside a container.
     * 
     * @param url
     *            the {@code url} of the container to list
     * @param options
     *            the options to specified to the list request. See
     *            {@linkplain ListOptions} documentation
     * @return an {@link ObjectList} matching the specified parameters.
     * @throws SdsException
     *             if any error occurs during request execution
     */
    public ObjectList listContainer(OioUrl url, ListOptions options)
            throws SdsException {
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

    /**
     * Deletes a container from the OpenIO namespace. The container should be
     * empty to be destroyed.
     * 
     * @param url
     *            the {@code url} of the container to destroy
     * @throws SdsException
     *             if any error occurs during request execution
     */
    public void deleteContainer(OioUrl url) throws SdsException {
        http.post(format(DELETE_CONTAINER_FORMAT,
                settings.url(), settings.ns(), url.account(),
                url.container()))
                .verifier(CONTAINER_VERIFIER)
                .execute()
                .close();
    }

    /**
     * Prepares an object upload by asking some chunks available location.
     * 
     * @param url
     *            the url of the future object to create
     * @param size
     *            the size of the future object
     * @return an {@link ObjectInfo} which contains all informations to upload
     *         the object
     * @throws SdsException
     *             if any error occurs during request execution
     */
    public ObjectInfo getBeans(OioUrl url, long size) throws SdsException {
        OioHttpResponse resp = http.post(
                format(GET_BEANS_FORMAT,
                        settings.url(), settings.ns(), url.account(),
                        url.container(), url.object()))
                .body(gson().toJson(new BeansRequest().size(size)))
                .verifier(OBJECT_VERIFIER)
                .execute();
        return objectInfoAndClose(url, resp);
    }

    /**
     * Validates an object upload in the OpenIO namespace
     * 
     * @param oinf
     *            the {@link ObjectInfo} containing informations about the
     *            uploaded object
     * @return the validated object.
     * @throws SdsException
     *             if any error occurs during request execution
     */
    public ObjectInfo putObject(ObjectInfo oinf) throws SdsException {
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

    /**
     * Returns informations about the specified object
     * 
     * @param url
     *            the url of the object to look for
     * @return an {@link ObjectInfo} containing informations about the object
     * @throws SdsException
     *             if any error occurs during request execution
     */
    public ObjectInfo getObjectInfo(OioUrl url) throws SdsException {
        OioHttpResponse resp = http.get(
                format(GET_OBJECT_FORMAT,
                        settings.url(), settings.ns(), url.account(),
                        url.container(), url.object()))
                .verifier(OBJECT_VERIFIER)
                .execute();
        return objectInfoAndClose(url, resp);
    }

    /**
     * Deletes an object from its container
     * 
     * @param url
     *            the url of the object to delete
     * @throws SdsException
     *             if any error occurs during request execution
     */
    public void deleteObject(OioUrl url) throws SdsException {
        http.post(
                format(DELETE_OBJECT_FORMAT,
                        settings.url(), settings.ns(), url.account(),
                        url.container(), url.object()))
                .verifier(OBJECT_VERIFIER)
                .execute()
                .close();
    }

    /* -- INTERNALS -- */

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

    private <T> List<T> listAndClose(OioHttpResponse resp) {
        try {
            Type t = new TypeToken<List<T>>() {
            }.getType();
            return gson().fromJson(new JsonReader(
                    new InputStreamReader(resp.body(), OIO_CHARSET)), t);
        } catch (Exception e) {
            throw new SdsException("Body extraction error", e);
        } finally {
            resp.close();
        }
    }
    
    private List<ServiceInfo> serviceInfoListAndClose(OioHttpResponse resp) {
        try {
            Type t = new TypeToken<List<ServiceInfo>>() {
            }.getType();
            return gson().fromJson(new JsonReader(
                    new InputStreamReader(resp.body(), OIO_CHARSET)), t);
        } catch (Exception e) {
            throw new SdsException("Body extraction error", e);
        } finally {
            resp.close();
        }
    }
    
    
    private static final OioHttpResponseVerifier REFERENCE_VERIFIER = new OioHttpResponseVerifier() {

        @Override
        public void verify(OioHttpResponse resp) throws SdsException {
            switch (resp.code()) {
            case 200:
            case 201:
            case 204:
                return;
            case 202:
                throw new ReferenceAlreadyExistException(resp.msg());
            case 400:
                throw new BadRequestException(resp.msg());
            case 404:
                throw new ReferenceNotFoundException(resp.msg());
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
