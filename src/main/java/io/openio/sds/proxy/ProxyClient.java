package io.openio.sds.proxy;

import static io.openio.sds.common.Check.checkArgument;
import static io.openio.sds.common.JsonUtils.gson;
import static io.openio.sds.common.JsonUtils.gsonForObject;
import static io.openio.sds.common.JsonUtils.jsonFromMap;
import static io.openio.sds.common.OioConstants.ACCOUNT_HEADER;
import static io.openio.sds.common.OioConstants.ACTION_MODE_HEADER;
import static io.openio.sds.common.OioConstants.CONTAINER_DEL_PROP;
import static io.openio.sds.common.OioConstants.CONTAINER_GET_PROP;
import static io.openio.sds.common.OioConstants.CONTAINER_SET_PROP;
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
import static io.openio.sds.common.OioConstants.DELETE_CONTAINER_FORMAT;
import static io.openio.sds.common.OioConstants.DELETE_OBJECT_FORMAT;
import static io.openio.sds.common.OioConstants.DELETE_MARKER_PARAM;
import static io.openio.sds.common.OioConstants.DELIMITER_PARAM;
import static io.openio.sds.common.OioConstants.DIR_LINK_SRV_FORMAT;
import static io.openio.sds.common.OioConstants.DIR_LIST_SRV_FORMAT;
import static io.openio.sds.common.OioConstants.DIR_REF_CREATE_FORMAT;
import static io.openio.sds.common.OioConstants.DIR_REF_DELETE_FORMAT;
import static io.openio.sds.common.OioConstants.DIR_REF_SHOW_FORMAT;
import static io.openio.sds.common.OioConstants.DIR_UNLINK_SRV_FORMAT;
import static io.openio.sds.common.OioConstants.FLUSH_PARAM;
import static io.openio.sds.common.OioConstants.GET_BEANS_FORMAT;
import static io.openio.sds.common.OioConstants.GET_CONTAINER_INFO_FORMAT;
import static io.openio.sds.common.OioConstants.GET_OBJECT_FORMAT;
import static io.openio.sds.common.OioConstants.INVALID_OBJECT_INFO_MSG;
import static io.openio.sds.common.OioConstants.INVALID_OPTIONS_MSG;
import static io.openio.sds.common.OioConstants.INVALID_URL_MSG;
import static io.openio.sds.common.OioConstants.LIST_MARKER_HEADER;
import static io.openio.sds.common.OioConstants.LIST_OBJECTS_FORMAT;
import static io.openio.sds.common.OioConstants.LIST_TRUNCATED_HEADER;
import static io.openio.sds.common.OioConstants.M2_CTIME_HEADER;
import static io.openio.sds.common.OioConstants.M2_INIT_HEADER;
import static io.openio.sds.common.OioConstants.M2_USAGE_HEADER;
import static io.openio.sds.common.OioConstants.M2_VERSION_HEADER;
import static io.openio.sds.common.OioConstants.MARKER_PARAM;
import static io.openio.sds.common.OioConstants.MAX_PARAM;
import static io.openio.sds.common.OioConstants.NS_HEADER;
import static io.openio.sds.common.OioConstants.OBJECT_DEL_PROP;
import static io.openio.sds.common.OioConstants.OBJECT_GET_PROP;
import static io.openio.sds.common.OioConstants.OBJECT_SET_PROP;
import static io.openio.sds.common.OioConstants.OIO_ACTION_MODE_HEADER;
import static io.openio.sds.common.OioConstants.OIO_CHARSET;
import static io.openio.sds.common.OioConstants.PREFIX_PARAM;
import static io.openio.sds.common.OioConstants.PUT_OBJECT_FORMAT;
import static io.openio.sds.common.OioConstants.SCHEMA_VERSION_HEADER;
import static io.openio.sds.common.OioConstants.SIMULATE_VERSIONING_HEADER;
import static io.openio.sds.common.OioConstants.TYPE_HEADER;
import static io.openio.sds.common.OioConstants.USER_NAME_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_ADMIN_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_ALIASES_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_CHUNKS_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_CONTENTS_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_MAIN_PROPERTIES_HEADER;
import static io.openio.sds.common.OioConstants.VERSION_PARAM;
import static io.openio.sds.common.Strings.nullOrEmpty;
import static io.openio.sds.http.OioHttpHelper.longHeader;
import static io.openio.sds.http.Verifiers.CONTAINER_VERIFIER;
import static io.openio.sds.http.Verifiers.OBJECT_VERIFIER;
import static io.openio.sds.http.Verifiers.REFERENCE_VERIFIER;
import static io.openio.sds.http.Verifiers.STANDALONE_VERIFIER;
import static java.lang.String.format;
import io.openio.sds.RequestContext;
import io.openio.sds.common.JsonUtils;
import io.openio.sds.common.OioConstants;
import io.openio.sds.common.Strings;
import io.openio.sds.exceptions.ContainerExistException;
import io.openio.sds.exceptions.ContainerNotFoundException;
import io.openio.sds.exceptions.ObjectNotFoundException;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.exceptions.OioSystemException;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttpResponse;
import io.openio.sds.http.OioHttp.RequestBuilder;
import io.openio.sds.models.BeansRequest;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.LinkedServiceInfo;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.NamespaceInfo;
import io.openio.sds.models.ObjectCreationOptions;
import io.openio.sds.models.ObjectDeletionOptions;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.ObjectList;
import io.openio.sds.models.OioUrl;
import io.openio.sds.models.ReferenceInfo;
import io.openio.sds.models.ServiceInfo;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * Simple OpenIO proxyd http client based on API reference available at
 * https://github.com/open-io/oio-sds/wiki/OpenIO-Proxyd-API-Reference
 */
public class ProxyClient {

    private final static String CONTENT_HEADER_PREFIX = "x-oio-content-meta-";
    private final static List<String> SYSMETA_KEYS = Arrays.asList(
            "chunk-method", "ctime", "mtime", "deleted", "hash",
            "hash-method", "id", "length", "mime-type", "name", "policy",
            "size", "version");

    private OioHttp http;
    private ProxySettings settings;
    private List<InetSocketAddress> hosts = null;

    public ProxyClient(OioHttp http, ProxySettings settings) {
        this.http = http;
        this.settings = settings;
        this.hosts = this.settings.allHosts();
    }

    /* -- CS -- */

    /**
     * Retrieves technical informations relative to the proxyd served namespace
     *
     * @param reqCtx
     *            common parameters to all requests
     * @return the matching {@code NamespaceInfo}
     */
    public NamespaceInfo getNamespaceInfo(RequestContext reqCtx) throws OioException {
        return http.get(format(CS_NSINFO_FORMAT, settings.url(), settings.ns()))
                .hosts(hosts).verifier(STANDALONE_VERIFIER)
                .withRequestContext(reqCtx).execute(NamespaceInfo.class);
    }

    /**
     * Returns available services of the specified type
     * 
     * @param type
     *            the type of service to get
     * @param reqCtx
     *            common parameters to all requests
     * @return the list of matching services
     * @throws OioException
     *             if any error occurs during request execution
     */
    public List<ServiceInfo> getServices(String type, RequestContext reqCtx) throws OioException {
        OioHttpResponse resp = http.get(
                format(CS_GETSRV_FORMAT, settings.url(), settings.ns(), type))
                .hosts(hosts).verifier(STANDALONE_VERIFIER)
                .withRequestContext(reqCtx).execute();
        return serviceInfoListAndClose(resp);
    }

    /* -- DIRECTORY -- */

    /**
     * Creates a reference in Oio directory.
     * 
     * @param url
     *            the url of the reference to create
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void createReference(OioUrl url) throws OioException {
        createReference(url, new RequestContext());
    }

    /**
     * Creates a reference in Oio directory.
     * 
     * @param url
     *            the url of the reference
     * @param reqCtx
     *            common parameters to all requests
     * @throws OioException
     *             if any error occurs during request execution
     */
    public void createReference(OioUrl url, RequestContext reqCtx)
            throws OioException {
        checkArgument(null != url, INVALID_URL_MSG);
        http.post(
                format(DIR_REF_CREATE_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container())))
                .hosts(hosts).verifier(REFERENCE_VERIFIER)
                .withRequestContext(reqCtx).execute().close();
    }

    /**
     * Retrieves informations about the specified reference
     * 
     * @param url
     *            the url of the reference to look for.
     * @return informations about the reference
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public ReferenceInfo showReference(OioUrl url) throws OioException {
        return showReference(url, new RequestContext());
    }

    /**
     * Retrieves informations about the specified reference
     * 
     * @param url
     *            the url of the reference to look for.
     * @param reqCtx
     *            common parameters to all requests
     * @return informations about the reference
     * @throws OioException
     *             if any error occurs during request execution
     */
    public ReferenceInfo showReference(OioUrl url, RequestContext reqCtx)
            throws OioException {
        checkArgument(null != url, INVALID_URL_MSG);
        return http.get(
                format(DIR_REF_SHOW_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container())))
                .hosts(hosts).verifier(REFERENCE_VERIFIER)
                .withRequestContext(reqCtx).execute(ReferenceInfo.class);
    }

    /**
     * Deletes a reference from Oio directory. Reference should not be linked to
     * a any service to be dropped
     * 
     * @param url
     *            the url of the reference
     * @throws OioException
     *             if any error occurs during request execution
     * 
     */
    @Deprecated
    public void deleteReference(OioUrl url) throws OioException {
        deleteReference(url, new RequestContext());
    }

    /**
     * Deletes a reference from Oio directory. Reference should not be linked to
     * a any service to be dropped
     * 
     * @param url
     *            the url of the reference
     * @param reqCtx
     *            common parameters to all requests
     * @throws OioException
     *             if any error occurs during request execution
     * 
     */
    public void deleteReference(OioUrl url, RequestContext reqCtx)
            throws OioException {
        checkArgument(null != url, INVALID_URL_MSG);
        http.post(
                format(DIR_REF_DELETE_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container())))
                .hosts(hosts).verifier(REFERENCE_VERIFIER)
                .withRequestContext(reqCtx).execute().close();
    }

    /**
     * Attachs a service of the specified type to a reference
     * 
     * @param url
     *            the url of the reference
     * @param type
     *            the type of service to link
     * @return the linked services
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public List<LinkedServiceInfo> linkService(OioUrl url, String type)
            throws OioException {
        return linkService(url, type, new RequestContext());
    }

    /**
     * Attachs a service of the specified type to a reference
     * 
     * @param url
     *            the url of the reference
     * @param type
     *            the type of service to link
     * @param reqCtx
     *            common parameters to all requests
     * @return the linked services
     * @throws OioException
     *             if any error occurs during request execution
     */
    public List<LinkedServiceInfo> linkService(OioUrl url, String type,
            RequestContext reqCtx) throws OioException {
        checkArgument(!nullOrEmpty(type), "Missing type");
        OioHttpResponse resp = http.post(
                format(DIR_LINK_SRV_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container()), type))
                .hosts(hosts).verifier(REFERENCE_VERIFIER)
                .withRequestContext(reqCtx).execute();

        return listAndClose(resp);
    }

    /**
     * Retrieves informations about the specified reference
     * 
     * @param url
     *            the url of the reference to look for.
     * @param type
     *            the type of service to list. Could be {@code null} to list all
     *            services
     * @return the linked services
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public List<LinkedServiceInfo> listServices(OioUrl url, String type)
            throws OioException {
        return listServices(url, type, new RequestContext());
    }

    /**
     * Retrieves informations about the specified reference
     * 
     * @param url
     *            the url of the reference to look for.
     * @param type
     *            the type of service to list. Could be {@code null} to list all
     *            services
     * @param reqCtx
     *            common parameters to all requests
     * @return the linked services
     * @throws OioException
     *             if any error occurs during request execution
     */
    public List<LinkedServiceInfo> listServices(OioUrl url, String type,
            RequestContext reqCtx) throws OioException {
        checkArgument(null != url, INVALID_URL_MSG);
        checkArgument(!nullOrEmpty(type));
        return http.get(
                format(DIR_LIST_SRV_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container()), type))
                .hosts(hosts).verifier(REFERENCE_VERIFIER)
                .withRequestContext(reqCtx).execute(ReferenceInfo.class).srv();
    }

    /**
     * Detach services from the specified URL
     * 
     * @param url
     *            the url of the reference
     * @param type
     *            the service to unlink
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void unlinkService(OioUrl url, String type) throws OioException {
        unlinkService(url, type, new RequestContext());
    }

    /**
     * Detachs services from the specified url
     * 
     * @param url
     *            the url of the reference
     * @param type
     *            the type of service to unlink
     * @param reqCtx
     *            common parameters to all requests
     * @throws OioException
     *             if any error occurs during request execution
     */
    public void unlinkService(OioUrl url, String type, RequestContext reqCtx)
            throws OioException {
        checkArgument(null != url, INVALID_URL_MSG);
        checkArgument(!nullOrEmpty(type));
        http.post(
                format(DIR_UNLINK_SRV_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container()), type))
                .hosts(hosts).verifier(REFERENCE_VERIFIER)
                .withRequestContext(reqCtx).execute().close();

    }

    /* -- STORAGE -- */

    /**
     * Creates a container using the specified {@code OioUrl}
     *
     * @param url
     *            the url of the container to create
     * @return {@code ContainerInfo}
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public ContainerInfo createContainer(OioUrl url) throws OioException {
        return createContainer(url, null, null, new RequestContext());
    }

    /**
     * Creates a container using the specified {@code OioUrl}
     *
     * @param url
     *            the url of the container to create
     * @param reqCtx
     *            common parameters to all requests
     * @return {@code ContainerInfo}
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public ContainerInfo createContainer(OioUrl url, RequestContext reqCtx)
            throws OioException {
        return createContainer(url, null, null, reqCtx);
    }

    /**
     * Creates a container using the specified {@code OioUrl}
     *
     * @param url
     *            the url of the container to create
     * @param properties
     *            the user properties to set
     * @param reqCtx
     *            common parameters to all requests
     * @return {@code ContainerInfo}
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public ContainerInfo createContainer(OioUrl url,
            Map<String, String> properties, RequestContext reqCtx)
            throws OioException {
        return createContainer(url, properties, null, reqCtx);
    }

    /**
     * Creates a container using the specified {@code OioUrl}
     *
     * @param url
     *            the url of the container to create
     * @param properties
     *            the user properties to set
     * @param system
     *            the system properties to set
     * @param reqCtx
     *            common parameters to all requests
     * @return {@code ContainerInfo}
     * @throws OioException
     *             if any error occurs during request execution
     */
    public ContainerInfo createContainer(OioUrl url,
            Map<String, String> properties, Map<String, String> system,
            RequestContext reqCtx)
            throws OioException {
        checkArgument(null != url, INVALID_URL_MSG);
        String body = format("{\"properties\": %1$s, \"system\": %2$s}",
                (properties == null) ? "{}" : jsonFromMap(properties),
                (system == null) ? "{}" : jsonFromMap(system));
        OioHttpResponse resp = http.post(
                format(CREATE_CONTAINER_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container())))
                .header(OIO_ACTION_MODE_HEADER, "autocreate")
                .body(body)
                .hosts(hosts).verifier(CONTAINER_VERIFIER)
                .withRequestContext(reqCtx).execute().close();
        if (204 == resp.code())
            throw new ContainerExistException("Container already present");

        return new ContainerInfo(url.container());
    }

    /**
     * Returns informations about the specified container
     * 
     * @param url
     *            the url of the container
     * @return the container informations
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public ContainerInfo getContainerInfo(OioUrl url) throws OioException {
        return getContainerInfo(url, new RequestContext());
    }

    /**
     * Returns informations about the specified container
     * 
     * @param url
     *            the url of the container
     * @param reqCtx
     *            common parameters to all requests
     * @return the container informations
     * @throws OioException
     *             if any error occurs during request execution
     */
    public ContainerInfo getContainerInfo(OioUrl url, RequestContext reqCtx)
            throws OioException {
        checkArgument(null != url, INVALID_URL_MSG);
        OioHttpResponse resp = http.get(
                format(GET_CONTAINER_INFO_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container())))
                .hosts(hosts).verifier(CONTAINER_VERIFIER)
                .withRequestContext(reqCtx).execute().close();

        return new ContainerInfo(url.container())
                .account(resp.header(ACCOUNT_HEADER))
                .ctime(longHeader(resp, M2_CTIME_HEADER))
                .init(longHeader(resp, M2_INIT_HEADER))
                .usage(longHeader(resp, M2_USAGE_HEADER))
                .version(longHeader(resp, M2_VERSION_HEADER))
                .id(resp.header(CONTAINER_SYS_NAME_HEADER))
                .ns(resp.header(NS_HEADER))
                .type(resp.header(TYPE_HEADER))
                .user(resp.header(USER_NAME_HEADER))
                .schemavers(resp.header(SCHEMA_VERSION_HEADER))
                .versionMainAdmin(resp.header(VERSION_MAIN_ADMIN_HEADER))
                .versionMainAliases(resp.header(VERSION_MAIN_ALIASES_HEADER))
                .versionMainChunks(resp.header(VERSION_MAIN_CHUNKS_HEADER))
                .versionMainContents(resp.header(VERSION_MAIN_CONTENTS_HEADER))
                .versionMainProperties(resp.header(VERSION_MAIN_PROPERTIES_HEADER));
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
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public ObjectList listContainer(OioUrl url, ListOptions options)
            throws OioException {
        return listObjects(url, options, new RequestContext());
    }

    /**
     * Lists all object available inside a container.
     * 
     * @param url
     *            the {@code url} of the container to list
     * @param options
     *            the options to specified to the list request. See
     *            {@linkplain ListOptions} documentation
     * @param reqCtx
     *            common parameters to all requests
     * @return an {@link ObjectList} matching the specified parameters.
     * @throws OioException
     *             if any error occurs during request execution
     */
    public ObjectList listObjects(OioUrl url, ListOptions options,
            RequestContext reqCtx) throws OioException {
        checkArgument(null != url, INVALID_URL_MSG);
        checkArgument(null != options, "Invalid options");
        OioHttpResponse resp = http.get(
                format(LIST_OBJECTS_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container())))
                .query(MAX_PARAM, options.limit() > 0 ? String.valueOf(options.limit()) : null)
                .query(PREFIX_PARAM, options.prefix())
                .query(MARKER_PARAM, options.marker())
                .query(DELIMITER_PARAM, options.delimiter())
                .hosts(hosts).verifier(CONTAINER_VERIFIER)
                .withRequestContext(reqCtx).execute();
        boolean success = false;
        try {
            ObjectList objectList = gson().fromJson(
                    new JsonReader(new InputStreamReader(resp.body(),
                            OIO_CHARSET)),
                    ObjectList.class);
            String truncated = resp.header(LIST_TRUNCATED_HEADER);
            if (truncated != null) {
                objectList.truncated(Boolean.parseBoolean(truncated));
                objectList.nextMarker(resp.header(LIST_MARKER_HEADER));
            }
            success = true;
            return objectList;
        } finally {
            resp.close(success);
        }
    }

    /**
     * Deletes a container from the OpenIO namespace. The container should be
     * empty to be destroyed.
     * 
     * @param url
     *            the URL of the container to destroy
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void deleteContainer(OioUrl url) throws OioException {
        deleteContainer(url, new RequestContext());
    }

    /**
     * Deletes a container from the OpenIO namespace. The container should be
     * empty to be destroyed.
     * 
     * @param url
     *            the URL of the container to destroy
     * @param reqCtx
     *            common parameters to all requests
     * @throws OioException
     *             if any error occurs during request execution
     */
    public void deleteContainer(OioUrl url, RequestContext reqCtx)
            throws OioException {
        checkArgument(null != url, INVALID_URL_MSG);
        http.post(
                format(DELETE_CONTAINER_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container())))
                .hosts(hosts).verifier(CONTAINER_VERIFIER)
                .withRequestContext(reqCtx).execute().close();
    }

    /**
     * Prepares an object upload by asking some chunks available location.
     *
     * @param url
     *            the URL of the future object to create
     * @param size
     *            the size of the future object
     * @param reqCtx
     *            Common parameters to all requests
     * @return an {@link ObjectInfo} which contains all informations to upload
     *         the object
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public ObjectInfo preparePutObject(OioUrl url, long size,
            RequestContext reqCtx) throws OioException {
        ObjectCreationOptions options = new ObjectCreationOptions();
        return this.preparePutObject(url, size, options, reqCtx);
    }

    /**
     * Prepares an object upload by asking some chunks available location.
     *
     * @param url
     *            the URL of the future object to create
     * @param size
     *            the size of the future object
     * @param policy
     *            the policy of the future object
     * @param version
     *            the version of the future object
     * @param reqCtx
     *            Common parameters to all requests
     * @return an {@link ObjectInfo} which contains all informations to upload
     *         the object
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public ObjectInfo preparePutObject(OioUrl url, long size,
            String policy, Long version, RequestContext reqCtx)
            throws OioException {
        ObjectCreationOptions options = new ObjectCreationOptions()
                .policy(policy)
                .version(version);
        return this.preparePutObject(url, size, options, reqCtx);
    }

    /**
     * Prepares an object upload by asking some chunks available location.
     *
     * @param url
     *            the URL of the future object to create
     * @param size
     *            the size of the future object
     * @param options
     *            the options of the future object
     * @param reqCtx
     *            Common parameters to all requests
     * @return an {@link ObjectInfo} which contains all informations to upload
     *         the object
     * @throws OioException
     *             if any error occurs during request execution
     */
    public ObjectInfo preparePutObject(OioUrl url, long size,
            ObjectCreationOptions options, RequestContext reqCtx)
            throws OioException {
        checkArgument(url != null, INVALID_URL_MSG);
        checkArgument(options != null, INVALID_OPTIONS_MSG);
        BeansRequest beansRequest = new BeansRequest()
                .size(size)
                .policy(options.policy());
        RequestBuilder request = http.post(
                format(GET_BEANS_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container()),
                        Strings.urlEncode(url.object())));
        if (options.version() != null)
            request.query(VERSION_PARAM, options.version().toString());
        OioHttpResponse resp = request
                .header(ACTION_MODE_HEADER,
                        settings.autocreate() ? OioConstants.AUTOCREATE_ACTION_MODE : null)
                .body(gson().toJson(beansRequest))
                .hosts(hosts).verifier(OBJECT_VERIFIER)
                .withRequestContext(reqCtx).execute();
        return getBeansObjectInfoAndClose(url, resp);
    }

    /**
     * Validate an object upload in the OpenIO-SDS namespace.
     *
     * @param oinf
     *            the {@link ObjectInfo} containing informations about the
     *            uploaded object
     * @param version
     *            the version to set (could be {@code null} to the object
     * @param reqCtx
     *            Common parameters to all requests
     * @return the validated object.
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public ObjectInfo putObject(ObjectInfo oinf, Long version,
            RequestContext reqCtx) throws OioException {
        ObjectCreationOptions options = new ObjectCreationOptions()
                .version(version);
        return this.putObject(oinf, options, reqCtx);
    }

    /**
     * Validate an object upload in the OpenIO-SDS namespace.
     *
     * @param oinf
     *            the {@link ObjectInfo} containing informations about the
     *            uploaded object
     * @param reqCtx
     *            Common parameters to all requests
     * @return the validated object.
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public ObjectInfo putObject(ObjectInfo oinf, RequestContext reqCtx)
            throws OioException {
        ObjectCreationOptions options = new ObjectCreationOptions();
        return this.putObject(oinf, options, reqCtx);
    }

    /**
     * Validate an object upload in the OpenIO-SDS namespace.
     *
     * @param oinf
     *            the {@link ObjectInfo} containing informations about the
     *            uploaded object
     * @param options
     *            the options of the object to upload
     * @param reqCtx
     *            Common parameters to all requests
     * @return the validated object.
     * @throws OioException
     *             if any error occurs during request execution
     */
    public ObjectInfo putObject(ObjectInfo oinf, ObjectCreationOptions options,
            RequestContext reqCtx) throws OioException {
        checkArgument(oinf != null, INVALID_OBJECT_INFO_MSG);
        checkArgument(options != null, INVALID_OPTIONS_MSG);
        checkArgument(options.version() == null
                || options.version().equals(oinf.version()), "Invalid version");
        checkArgument(options.policy() == null
                || options.policy().equals(oinf.policy()), "Invalid policy");

        oinf.properties(options.properties());
        if (options.mimeType() != null)
            oinf.mimeType(options.mimeType());

        Map<String, String> props = oinf.properties();
        String body = format("{\"chunks\": %1$s, \"properties\": %2$s}",
                gsonForObject().toJson(oinf.chunks()),
                props != null ? jsonFromMap(props) : "{}");
        RequestBuilder request = http.post(
                format(PUT_OBJECT_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(oinf.url().account()),
                        Strings.urlEncode(oinf.url().container()),
                        Strings.urlEncode(oinf.url().object())))
                .header(CONTENT_META_CHUNK_METHOD_HEADER, oinf.chunkMethod())
                .header(CONTENT_META_HASH_HEADER, oinf.hash())
                .header(CONTENT_META_ID_HEADER, oinf.oid())
                .header(CONTENT_META_LENGTH_HEADER, String.valueOf(oinf.size()))
                .header(CONTENT_META_ID_HEADER, oinf.oid())
                .header(CONTENT_META_MIME_TYPE_HEADER, oinf.mimeType())
                .header(CONTENT_META_POLICY_HEADER, oinf.policy())
                .header(CONTENT_META_VERSION_HEADER, oinf.version().toString())
                .header(ACTION_MODE_HEADER,
                        settings.autocreate() ? OioConstants.AUTOCREATE_ACTION_MODE : null);
        if (options.simulateVersioning())
            request.header(SIMULATE_VERSIONING_HEADER, "1");
        request.body(body)
                .hosts(hosts).verifier(OBJECT_VERIFIER)
                .withRequestContext(reqCtx).execute().close();
        return oinf;
    }

    /**
     * Returns informations about the specified object
     * 
     * @param url
     *            the url of the object to look for
     * @return an {@link ObjectInfo} containing informations about the object
     */
    @Deprecated
    public ObjectInfo getObjectInfo(OioUrl url) throws OioException {
        return getObjectInfo(url, true);
    }

    /**
     * Returns informations about the specified object
     *
     * @param url
     *            the url of the object to look for
     * @param loadProperties
     *            Whether or not to load properties
     * @return an {@link ObjectInfo} containing informations about the object
     */
    @Deprecated
    public ObjectInfo getObjectInfo(OioUrl url, boolean loadProperties)
            throws OioException {
        return getObjectInfo(url, null, loadProperties);
    }

    /**
     * Returns informations about the specified object
     * 
     * @param url
     *            the url of the object to look for
     * @param version
     *            the version of the content to get
     * @return an {@link ObjectInfo} containing informations about the object
     */
    @Deprecated
    public ObjectInfo getObjectInfo(OioUrl url, Long version)
            throws OioException {
        return getObjectInfo(url, version, true);
    }

    /**
     * Returns informations about the specified object
     *
     * @param url
     *            the url of the object to look for
     * @param version
     *            the version of the content to get
     * @param loadProperties
     *            Whether or not to load properties
     * @return an {@link ObjectInfo} containing informations about the object
     */
    @Deprecated
    public ObjectInfo getObjectInfo(OioUrl url, Long version,
            boolean loadProperties) throws OioException {
        return getObjectInfo(url, version, new RequestContext(), loadProperties);
    }

    /**
     * Returns informations about the specified object
     * 
     * @param url
     *            the url of the object to look for
     * @param version
     *            the version to get (could be {@code null} to get latest
     *            version)
     * @param reqCtx
     *            common parameters to all requests
     * @return an {@link ObjectInfo} containing informations about the object
     */
    public ObjectInfo getObjectInfo(OioUrl url, Long version,
            RequestContext reqCtx) throws OioException {
        return getObjectInfo(url, version, reqCtx, true);
    }

    /**
     * Returns informations about the specified object
     *
     * @param url
     *            the url of the object to look for
     * @param version
     *            the version to get (could be {@code null} to get latest
     *            version)
     * @param reqCtx
     *            common parameters to all requests
     * @param loadProperties
     *            Whether or not to load properties
     * @return an {@link ObjectInfo} containing informations about the object
     */
    public ObjectInfo getObjectInfo(OioUrl url, Long version,
            RequestContext reqCtx, boolean loadProperties) throws OioException {
        checkArgument(null != url, INVALID_URL_MSG);
        RequestBuilder request = http.get(
                format(GET_OBJECT_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container()),
                        Strings.urlEncode(url.object())));
        if (version != null)
            request.query(VERSION_PARAM, version.toString());
        OioHttpResponse resp = request.hosts(hosts).verifier(OBJECT_VERIFIER)
                .withRequestContext(reqCtx).execute();
        ObjectInfo info = objectShowObjectInfoAndClose(url, resp, loadProperties);
        return info;
    }

    /**
     * Deletes an object from its container
     *
     * @param url
     *            the url of the object to delete
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void deleteObject(OioUrl url) throws OioException {
        ObjectDeletionOptions options = new ObjectDeletionOptions();
        this.deleteObject(url, options, new RequestContext());
    }

    /**
     * Deletes an object from its container
     *
     * @param url
     *            the url of the object to delete
     * @param version
     *            the version to delete (could be {@code null} to delete latest
     *            version)
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void deleteObject(OioUrl url, Long version) throws OioException {
        ObjectDeletionOptions options = new ObjectDeletionOptions()
                .version(version);
        this.deleteObject(url, options, new RequestContext());
    }

    /**
     * Deletes an object from its container
     *
     * @param url
     *            the url of the object to delete
     * @param version
     *            the version to delete (could be {@code null} to delete latest
     *            version)
     * @param reqCtx
     *            common paramters to all requests
     * @throws OioException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void deleteObject(OioUrl url, Long version, RequestContext reqCtx)
            throws OioException {
        ObjectDeletionOptions options = new ObjectDeletionOptions()
                .version(version);
        this.deleteObject(url, options, new RequestContext());
    }

    /**
     * Deletes an object from its container
     *
     * @param url
     *            the url of the object to delete
     * @param options
     *            the version to delete (could be {@code null} to delete latest
     *            version)
     * @param reqCtx
     *            common paramters to all requests
     * @throws OioException
     *             if any error occurs during request execution
     */
    public void deleteObject(OioUrl url, ObjectDeletionOptions options,
            RequestContext reqCtx) throws OioException {
        checkArgument(url != null, INVALID_URL_MSG);
        checkArgument(options != null, INVALID_OPTIONS_MSG);
        RequestBuilder request = http.post(
                format(DELETE_OBJECT_FORMAT, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container()),
                        Strings.urlEncode(url.object())));
        if (options.version() != null)
            request.query(VERSION_PARAM, options.version().toString());
        if (options.deleteMarker())
            request.query(DELETE_MARKER_PARAM, "1");
        if (options.simulateVersioning())
            request.header(SIMULATE_VERSIONING_HEADER, "1");
        request.hosts(hosts).verifier(OBJECT_VERIFIER)
                .withRequestContext(reqCtx).execute().close();
    }

    /* -- PROPERTIES -- */

    /**
     * Set properties to the specified container.
     *
     * @param url
     *            the URL of the container to set properties
     * @param properties
     *            the properties to set
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void setContainerProperties(OioUrl url,
            Map<String, String> properties) {
        setContainerProperties(url, properties, false, new RequestContext());
    }

    /**
     * Set properties to the specified container.
     *
     * @param url
     *            the URL of the container to set properties
     * @param properties
     *            the properties to set
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void setContainerProperties(OioUrl url,
            Map<String, String> properties, RequestContext reqCtx) {
        setContainerProperties(url, properties, false, reqCtx);
    }

    /**
     * Set properties to the specified container.
     *
     * @param url
     *            the URL of the container to set properties
     * @param properties
     *            the properties to set
     * @param clear
     *            clear previous properties
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void setContainerProperties(OioUrl url,
            Map<String, String> properties, boolean clear,
            RequestContext reqCtx) {
        setContainerProperties(url, properties, clear, null, reqCtx);
    }

    /**
     * Set properties to the specified container.
     *
     * @param url
     *            the URL of the container to set properties
     * @param properties
     *            the properties to set
     * @param system
     *            the system properties to set
     * @param clear
     *            clear previous properties
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void setContainerProperties(OioUrl url,
            Map<String, String> properties, boolean clear,
            Map<String, String> system, RequestContext reqCtx) {
        checkArgument(null != url, INVALID_URL_MSG);
        RequestBuilder request = http.post(
                format(CONTAINER_SET_PROP, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container())));
        if (clear)
            request.query(FLUSH_PARAM, "1");
        String body = format("{\"properties\": %1$s, \"system\": %2$s}",
                (properties == null) ? "{}" : jsonFromMap(properties),
                (system == null) ? "{}" : jsonFromMap(system));
        request.body(body)
                .hosts(hosts).verifier(CONTAINER_VERIFIER)
                .withRequestContext(reqCtx).execute().close();
    }

    /**
     * Retrieve user properties of the specified container.
     * 
     * @param url
     *            the url of the container
     * @return the user properties found on the container
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public Map<String, String> getContainerProperties(OioUrl url) {
        return getContainerProperties(url, new RequestContext());
    }

    /**
     * Retrieve user properties of the specified container.
     * 
     * @param url
     *            the url of the container
     * @param reqCtx
     *            common parameters to all requests
     * @return the user properties found on the container
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public Map<String, String> getContainerProperties(OioUrl url,
            RequestContext reqCtx) {
        return getAllContainerProperties(url, new RequestContext())
                .get("properties");
    }

    /**
     * Retrieve user properties and system properties
     * of the specified container.
     *
     * @param url
     *            the url of the container
     * @param reqCtx
     *            common parameters to all requests
     * @return the user properties and system properties found on the container
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public Map<String, Map<String, String>> getAllContainerProperties(
            OioUrl url, RequestContext reqCtx) {
        checkArgument(null != url, INVALID_URL_MSG);
        OioHttpResponse resp = http.post(
                format(CONTAINER_GET_PROP, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container())))
                .hosts(hosts).verifier(CONTAINER_VERIFIER)
                .withRequestContext(reqCtx).execute();
        try {
            return JsonUtils.jsonToMapMap(resp.body());
        } finally {
            resp.close();
        }
    }

    /**
     * Deletes user properties from the specified container
     * 
     * @param url
     *            the url of the container
     * @param keys
     *            the property keys to drop
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void deleteContainerProperties(OioUrl url, String... keys) {
        deleteContainerProperties(new RequestContext(), url, keys);
    }

    /**
     * Deletes user properties from the specified container
     *
     * @param reqCtx
     *            common parameters to all requests
     * @param url
     *            the url of the container
     * @param keys
     *            the property keys to drop
     * 
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void deleteContainerProperties(RequestContext reqCtx, OioUrl url,
            String... keys) {
        checkArgument(null != url, INVALID_URL_MSG);
        if (keys == null)
            keys = Strings.EMPTY_ARRAY;
        http.post(
                format(CONTAINER_DEL_PROP, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container())))
                .body(gson().toJson(keys))
                .hosts(hosts).verifier(CONTAINER_VERIFIER)
                .withRequestContext(reqCtx).execute().close();
    }

    /**
     * Deletes user properties from the specified container
     * 
     * @param url
     *            the url of the container
     * @param keys
     *            the property keys to drop
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void deleteContainerProperties(OioUrl url, List<String> keys) {
        deleteContainerProperties(url, keys, new RequestContext());
    }

    /**
     * Deletes user properties from the specified container
     * 
     * @param url
     *            the url of the container
     * @param keys
     *            the property keys to drop
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void deleteContainerProperties(OioUrl url, List<String> keys,
            RequestContext reqCtx) {
        checkArgument(null != url, INVALID_URL_MSG);
        if (keys == null)
            keys = Collections.emptyList();
        http.post(
                format(CONTAINER_DEL_PROP, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container())))
                .body(gson().toJson(keys))
                .hosts(hosts).verifier(CONTAINER_VERIFIER)
                .withRequestContext(reqCtx).execute().close();
    }

    /**
     * Add properties to the specified object. The properties must be prefixed
     * with "user." and this prefix will be stored, and finally used to query
     * the parameters later.
     *
     * @param url
     *            the URL of the object
     * @param properties
     *            the properties to set
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void setObjectProperties(OioUrl url, Map<String, String> properties) {
        setObjectProperties(url, null, properties, false, new RequestContext());
    }

    /**
     * Add properties to the specified object. The properties must be prefixed
     * with "user." and this prefix will be stored, and finally used to query
     * the parameters later.
     *
     * @param url
     *            the URL of the object
     * @param properties
     *            the properties to set
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void setObjectProperties(OioUrl url, Map<String, String> properties,
            RequestContext reqCtx) {
        setObjectProperties(url, null, properties, false, reqCtx);
    }

    /**
     * Add properties to the specified object. The properties must be prefixed
     * with "user." and this prefix will be stored, and finally used to query
     * the parameters later.
     *
     * @param url
     *            the URL of the object
     * @param properties
     *            the properties to set
     * @param clear
     *            clear previous properties
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void setObjectProperties(OioUrl url, Map<String, String> properties,
            boolean clear, RequestContext reqCtx) {
        setObjectProperties(url, null, properties, clear, reqCtx);
    }

    /**
     * Add properties to the specified object. The properties must be prefixed
     * with "user." and this prefix will be stored, and finally used to query
     * the parameters later.
     *
     * @param url
     *            the URL of the object
     * @param version
     *            the version to manipulate
     *            (could be {@code null} to manipulate latest version)
     * @param properties
     *            the properties to set
     * @param clear
     *            clear previous properties
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void setObjectProperties(OioUrl url, Long version,
            Map<String, String> properties, boolean clear,
            RequestContext reqCtx) {
        checkArgument(null != url && null != url.object(), INVALID_URL_MSG);
        checkArgument(null != properties && properties.size() > 0, "Invalid properties");
        String body = format("{\"properties\": %1$s}",
                jsonFromMap(properties));
        RequestBuilder request = http.post(
                format(OBJECT_SET_PROP, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container()),
                        Strings.urlEncode(url.object())));
        if (version != null)
            request.query(VERSION_PARAM, version.toString());
        if (clear)
            request.query(FLUSH_PARAM, "1");
        request.body(body)
                .hosts(hosts).verifier(OBJECT_VERIFIER)
                .withRequestContext(reqCtx).execute().close();
    }

    /**
     * Retrieve user properties of the specified object
     *
     * @param url
     *            the url of the object
     * @return the user properties (i.e. prefixed with "user.") found on the
     *         object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public Map<String, String> getObjectProperties(OioUrl url) {
        return getObjectProperties(url, null, new RequestContext());
    }

    /**
     * Retrieve user properties of the specified object
     *
     * @param url
     *            the url of the object
     * @param reqCtx
     *            common parameters to all requests
     * @return the user properties (i.e. prefixed with "user.") found on the
     *         object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public Map<String, String> getObjectProperties(OioUrl url,
            RequestContext reqCtx) {
        return getObjectProperties(url, null, reqCtx);
    }

    /**
     * Retrieve user properties of the specified object
     *
     * @param url
     *            the url of the object
     * @param version
     *            the version to manipulate
     *            (could be {@code null} to manipulate latest version)
     * @param reqCtx
     *            common parameters to all requests
     * @return the user properties (i.e. prefixed with "user.") found on the
     *         object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public Map<String, String> getObjectProperties(OioUrl url, Long version,
            RequestContext reqCtx) {
        checkArgument(null != url && null != url.object(), INVALID_URL_MSG);
        RequestBuilder request = http.post(
                format(OBJECT_GET_PROP, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container()),
                        Strings.urlEncode(url.object())));
        if (version != null)
            request.query(VERSION_PARAM, version.toString());
        OioHttpResponse resp = request
                .hosts(hosts).verifier(OBJECT_VERIFIER)
                .withRequestContext(reqCtx).execute();
        try {
            Map<String, Map<String, String>> rootMap = JsonUtils.jsonToMapMap(
                    resp.body());
            return rootMap.get("properties");
        } finally {
            resp.close();
        }
    }

    /**
     * Deletes the specified properties from the object
     * 
     * @param url
     *            the url of the object
     * @param keys
     *            the property keys to drop
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void deleteObjectProperties(OioUrl url, String... keys) {
        deleteObjectProperties(new RequestContext(), url, null, keys);
    }

    /**
     * Deletes the specified properties from the object
     * 
     * @param url
     *            the url of the object
     * @param keys
     *            the property keys to drop
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void deleteObjectProperties(RequestContext reqCtx, OioUrl url,
            String... keys) {
        deleteObjectProperties(reqCtx, url, null, keys);
    }

    /**
     * Deletes the specified properties from the object
     * 
     * @param url
     *            the url of the object
     * @param version
     *            the version to manipulate
     *            (could be {@code null} to manipulate latest version)
     * @param keys
     *            the property keys to drop
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void deleteObjectProperties(RequestContext reqCtx, OioUrl url,
            Long version, String... keys) {
        checkArgument(null != url && null != url.object(), INVALID_URL_MSG);
        String body = "[]";
        if (keys != null)
            body = gson().toJson(keys);
        RequestBuilder request = http.post(
                format(OBJECT_DEL_PROP, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container()),
                        Strings.urlEncode(url.object())));
        if (version != null)
            request.query(VERSION_PARAM, version.toString());
        request.body(body)
                .hosts(hosts).verifier(OBJECT_VERIFIER)
                .withRequestContext(reqCtx).execute().close();
    }

    /**
     * Deletes the specified properties from the object
     * 
     * @param url
     *            the url of the object
     * @param keys
     *            the property keys to drop
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void deleteObjectProperties(OioUrl url, List<String> keys) {
        deleteObjectProperties(url, null, keys, new RequestContext());
    }

    /**
     * Deletes the specified properties from the object
     * 
     * @param url
     *            the url of the object
     * @param keys
     *            the property keys to drop
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public void deleteObjectProperties(OioUrl url, List<String> keys,
            RequestContext reqCtx) {
        deleteObjectProperties(url, null, keys, reqCtx);
    }

    /**
     * Deletes the specified properties from the object
     * 
     * @param url
     *            the url of the object
     * @param version
     *            the version to manipulate
     *            (could be {@code null} to manipulate latest version)
     * @param keys
     *            the property keys to drop
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void deleteObjectProperties(OioUrl url, Long version,
            List<String> keys, RequestContext reqCtx) {
        checkArgument(null != url && null != url.object(), INVALID_URL_MSG);
        String body = "[]";
        if (keys != null)
            body = gson().toJson(keys);
        RequestBuilder request = http.post(
                format(OBJECT_DEL_PROP, settings.url(), settings.ns(),
                        Strings.urlEncode(url.account()),
                        Strings.urlEncode(url.container()),
                        Strings.urlEncode(url.object())));
        if (version != null)
            request.query(VERSION_PARAM, version.toString());
        request.body(body)
                .hosts(hosts).verifier(OBJECT_VERIFIER)
                .withRequestContext(reqCtx).execute().close();
    }

    /* -- INTERNALS -- */

    private ObjectInfo getBeansObjectInfoAndClose(OioUrl url,
            OioHttpResponse resp) {
        boolean success = false;
        try {
            ObjectInfo oinf = fillObjectInfo(url, resp, false);
            List<ChunkInfo> chunks = bodyChunk(resp);
            // check if we are using EC with ec daemon
            if (oinf.isEC()) {
                if (settings.ecdrain()) {
                    if (Strings.nullOrEmpty(settings.ecd()))
                        throw new OioException("Missing proxy#ecd configuration");
                } else {
                    // TODO impl EC in java
                    throw new IllegalStateException(
                            "Invalid configuration, we cannot do EC without ecd ATM");
                }
            }
            oinf.chunks(chunks);

            success = true;
            return oinf;
        } finally {
            resp.close(success);
        }
    }

    private ObjectInfo objectShowObjectInfoAndClose(OioUrl url,
            OioHttpResponse resp, boolean loadProperties) {
        boolean success = false;
        try {
            ObjectInfo oinf = fillObjectInfo(url, resp, loadProperties);
            List<ChunkInfo> chunks = bodyChunk(resp);
            // check if we are using EC with ecd
            if (oinf.chunkMethod().startsWith(OioConstants.EC_PREFIX)
                    && (!settings.ecdrain() || Strings.nullOrEmpty(settings.ecd())))
                throw new OioException("Unable to decode EC encoded object without ecd");
            oinf.chunks(chunks);
            success = true;
            return oinf;
        } finally {
            resp.close(success);
        }
    }

    private ObjectInfo fillObjectInfo(OioUrl url, OioHttpResponse r,
            boolean loadProperties) {
        ObjectInfo oinf = new ObjectInfo().url(url)
                .oid(r.header(CONTENT_META_ID_HEADER))
                .size(longHeader(r, CONTENT_META_LENGTH_HEADER))
                .ctime(longHeader(r, CONTENT_META_CTIME_HEADER))
                .chunkMethod(r.header(CONTENT_META_CHUNK_METHOD_HEADER))
                .policy(r.header(CONTENT_META_POLICY_HEADER))
                .version(longHeader(r, CONTENT_META_VERSION_HEADER))
                .hash(r.header(OioConstants.CONTENT_META_HASH_HEADER))
                .hashMethod(r.header(CONTENT_META_HASH_METHOD_HEADER))
                .mimeType(r.header(CONTENT_META_MIME_TYPE_HEADER));
        if (loadProperties)
            oinf.properties(propsFromHeaders(r.headers()));
        return oinf.withRequestContext(r.requestContext());
    }

    private List<ChunkInfo> bodyChunk(OioHttpResponse resp)
            throws OioException {
        try {
            return gson().fromJson(
                    new JsonReader(
                            new InputStreamReader(resp.body(), OIO_CHARSET)),
                    new TypeToken<List<ChunkInfo>>() {}.getType());
        } catch (Exception e) {
            throw new OioException("Body extraction error", e);
        }
    }

    private <T> List<T> listAndClose(OioHttpResponse resp) {
        boolean success = false;
        try {
            List<T> res = gson().fromJson(
                    new JsonReader(
                            new InputStreamReader(resp.body(), OIO_CHARSET)),
                    new TypeToken<List<T>>() {}.getType());
            success = true;
            return res;
        } catch (Exception e) {
            throw new OioException("Body extraction error", e);
        } finally {
            resp.close(success);
        }
    }

    private List<ServiceInfo> serviceInfoListAndClose(OioHttpResponse resp) {
        boolean success = false;
        try {
            List<ServiceInfo> res = gson().fromJson(
                    new JsonReader(
                        new InputStreamReader(resp.body(), OIO_CHARSET)),
                    new TypeToken<List<ServiceInfo>>() {}.getType());
            success = true;
            return res;
        } catch (Exception e) {
            throw new OioException("Body extraction error", e);
        } finally {
            resp.close(success);
        }
    }

    private Map<String, String> propsFromHeaders(HashMap<String,
            String> headers) {
        HashMap<String, String> props = new HashMap<String, String>();
        for (Entry<String, String> header : headers.entrySet()) {
            if (header.getKey().startsWith(CONTENT_HEADER_PREFIX)) {
                String shortKey = header.getKey()
                        .substring(CONTENT_HEADER_PREFIX.length());
                if (shortKey.startsWith("x-")
                        || !SYSMETA_KEYS.contains(shortKey)) {
                    try {
                        props.put(shortKey, URLDecoder.decode(header.getValue(),
                                StandardCharsets.UTF_8.toString()));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e.getCause());
                    }
                }
            }
        }
        return props;
    }
}
