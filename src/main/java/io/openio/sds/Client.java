package io.openio.sds;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import io.openio.sds.exceptions.ContainerExistException;
import io.openio.sds.exceptions.ContainerNotEmptyException;
import io.openio.sds.exceptions.ContainerNotFoundException;
import io.openio.sds.exceptions.ObjectExistException;
import io.openio.sds.exceptions.ObjectNotFoundException;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.exceptions.OioSystemException;
import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.NamespaceInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.ObjectList;
import io.openio.sds.models.OioUrl;
import io.openio.sds.models.Range;

/**
 * <p>
 * Contains all convenience methods to handle containers and objects in OpenIO
 * SDS
 * </p>
 * <p>
 * Instances of {@code Client} implementations are built with
 * {@link ClientBuilder}.
 * </p>
 * <p>
 * Simple example:
 * 
 * <code>
 * Client client = ClientBuilder.newClient("OPENIO", "http://127.0.0.1:6000");
 * </code>
 * 
 * </p>
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public interface Client {

    /**
     * Get OpenIO-SDS namespace configuration.
     *
     * @return the namespace configuration
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public NamespaceInfo getNamespaceInfo() throws OioException;

    /**
     * Get OpenIO-SDS namespace configuration.
     *
     * @param reqCtx
     *            Common parameters to all requests
     *
     * @return the namespace configuration
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public NamespaceInfo getNamespaceInfo(RequestContext reqCtx) throws OioException;

    /**
     * Create a container using the specified {@link OioUrl}. OioUrl objects are
     * built by using {@link OioUrl#url(String, String)} method, then you have
     * to specify the name of the account to use and the name of the future
     * container.
     * <p>
     * The container is available when the returned future is completed.
     * 
     * @param url
     *            the url of the container
     * @return a {@code ContainerInfo} with information about the created
     *         container
     * @throws ContainerExistException
     *             if the specified container is alreay present
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ContainerInfo createContainer(OioUrl url) throws OioException;

    /**
     * Create a container using the specified {@link OioUrl}. OioUrl objects are
     * built by using {@link OioUrl#url(String, String)} method, then you have
     * to specify the name of the account to use and the name of the future
     * container.
     * <p>
     * The container is available when the returned future is completed.
     *
     * @param url
     *            the URL of the container
     * @param reqCtx
     *            common parameters to all requests
     * @return a {@code ContainerInfo} with information about the created
     *         container
     * @throws ContainerExistException
     *             if the specified container is alreay present
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ContainerInfo createContainer(OioUrl url, RequestContext reqCtx) throws OioException;

    /**
     * Return information about the specified container.
     *
     * @param url
     *            the URL of the container
     * @return a {@code ContainerInfo} with information about the created
     *         container
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * 
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ContainerInfo getContainerInfo(OioUrl url) throws OioException;

    /**
     * Return information about the specified container.
     *
     * @param url
     *            the URL of the container
     * @param reqCtx
     *            common parameters to all requests
     * @return a {@code ContainerInfo} with information about the created
     *         container
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     *
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ContainerInfo getContainerInfo(OioUrl url, RequestContext reqCtx) throws OioException;

    /**
     * List object available in the specified container
     * 
     * @param url
     *            the url of the container
     * @param listOptions
     *            the listing option
     * 
     * @return the ObjectList
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    @Deprecated
    public ObjectList listContainer(OioUrl url, final ListOptions listOptions) throws OioException;

    /**
     * List objects available in the specified container.
     *
     * @param url
     *            the url of the container
     * @param listOptions
     *            the listing option
     * @return the ObjectList
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     * @since 1.0.0
     */
    public ObjectList listObjects(OioUrl url, final ListOptions listOptions) throws OioException;

    /**
     * List objects available in the specified container.
     *
     * @param url
     *            the url of the container
     * @param listOptions
     *            the listing option
     * @param reqCtx
     *            common parameters to all requests
     *
     * @return the ObjectList
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     * @since 1.0.0
     */
    public ObjectList listObjects(OioUrl url, final ListOptions listOptions, RequestContext reqCtx)
            throws OioException;

    /**
     * Deletes the specified container
     * 
     * @param url
     *            the url of the container
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ContainerNotEmptyException
     *             if the specified container isn't empty
     * 
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void deleteContainer(OioUrl url) throws OioException;

    /**
     * Delete the specified container
     *
     * @param url
     *            the URL of the container
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ContainerNotEmptyException
     *             if the specified container isn't empty
     *
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void deleteContainer(OioUrl url, RequestContext reqCtx) throws OioException;

    /**
     * Add properties to the specified container. The properties must be
     * prefixed with "user." and this prefix will be stored, and finally used to
     * query the parameters later
     * 
     * @param url
     *            the url of the container to add properties
     * @param props
     *            the properties to add
     * 
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void setContainerProperties(OioUrl url, Map<String, String> props) throws OioException;

    /**
     * Add properties to the specified container. The properties must be
     * prefixed with "user." and this prefix will be stored, and finally used to
     * query the parameters later
     * 
     * @param url
     *            the url of the container to add properties
     * @param props
     *            the properties to add
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void setContainerProperties(OioUrl url, Map<String, String> props, RequestContext reqCtx)
            throws OioException;

    /**
     * Retrieves user properties of the specified container
     * 
     * @param url
     *            the url of the object
     * @return the user properties (i.e. prefixed with "user.") found on the
     *         object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public Map<String, String> getContainerProperties(OioUrl url) throws OioException;

    /**
     * Retrieves user properties of the specified container
     * 
     * @param url
     *            the url of the object
     * @param reqCtx
     *            common parameters to all requests
     * @return the user properties (i.e. prefixed with "user.") found on the
     *         object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public Map<String, String> getContainerProperties(OioUrl url, RequestContext reqCtx)
            throws OioException;

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
    public void deleteContainerProperties(OioUrl url, String... keys) throws OioException;

    /**
     * Deletes user properties from the specified container
     * 
     * @param url
     *            the url of the container
     * @param reqCtx
     *            common parameters to all requests
     * @param keys
     *            the property keys to drop
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void deleteContainerProperties(OioUrl url, RequestContext reqCtx, String... keys)
            throws OioException;

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
    public void deleteContainerProperties(OioUrl url, List<String> keys) throws OioException;

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
    public void deleteContainerProperties(OioUrl url, List<String> keys, RequestContext reqCtx)
            throws OioException;

    /**
     * Push an object into the oio namespace
     * 
     * @param url
     *            the url of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the file to read the data from
     * @return information about the uploaded object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo putObject(OioUrl url, Long size, File data) throws OioException;

    /**
     * Push an object into the oio namespace
     * 
     * @param url
     *            the url of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the file to read the data from
     * @param properties
     *            the properties to set to the object. Note that the properties
     *            are case insensitive and will always be returned in lower
     *            case.
     * @return information about the uploaded object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo putObject(OioUrl url, Long size, File data, Map<String, String> properties)
            throws OioException;

    /**
     * Push an object into the oio namespace
     * 
     * @param url
     *            the url of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the file to read the data from
     * @param version
     *            the version of content to upload
     * @return information about the uploaded object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo putObject(OioUrl url, Long size, File data, Long version) throws OioException;

    /**
     * Push an object into the oio namespace
     * 
     * @param url
     *            the url of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the file to read the data from
     * @param version
     *            the version of content to upload
     * @param properties
     *            the properties to set to the object. Note that the properties
     *            are case insensitive and will always be returned in lower
     *            case.
     * @return information about the uploaded object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo putObject(OioUrl url, Long size, File data, Long version,
            Map<String, String> properties) throws OioException;

    /**
     * Push an object into the oio namespace
     * 
     * @param url
     *            the url of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the InputStream to read the data from
     * @return information about the uploaded object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectExistException
     *             if the specified object alreadeay exist in the container
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo putObject(OioUrl url, Long size, InputStream data) throws OioException;

    /**
     * Push an object into the oio namespace
     * 
     * @param url
     *            the url of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the InputStream to read the data from
     * @param properties
     *            the properties to set to the object. Note that the properties
     *            are case insensitive and will always be returned in lower
     *            case.
     * @return information about the uploaded object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectExistException
     *             if the specified object alreadeay exist in the container
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo putObject(OioUrl url, Long size, InputStream data,
            Map<String, String> properties) throws OioException;

    /**
     * Push an object into the oio namespace
     * 
     * @param url
     *            the url of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the InputStream to read the data from
     * @param version
     *            the version of content to upload
     * @return information about the uploaded object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectExistException
     *             if the specified object alreadeay exist in the container
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo putObject(OioUrl url, Long size, InputStream data, Long version)
            throws OioException;

    /**
     * Push an object into the oio namespace
     * 
     * @param url
     *            the url of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the InputStream to read the data from
     * @param version
     *            the version of content to upload
     * @param properties
     *            the properties to set to the object. Note that the properties
     *            are case insensitive and will always be returned in lower
     *            case.
     * @return information about the uploaded object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectExistException
     *             if the specified object alreadeay exist in the container
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo putObject(OioUrl url, Long size, InputStream data, Long version,
            Map<String, String> properties) throws OioException;

    /**
     * Push an object into the OpenIO-SDS namespace.
     *
     * @param url
     *            the URL of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the InputStream to read the data from
     * @param version
     *            the version of content to upload
     * @param properties
     *            the properties to set to the object. Note that the properties
     *            are case insensitive and will always be returned in lower
     *            case.
     * @param reqCtx
     *            Common parameters to all requests
     * @return information about the uploaded object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectExistException
     *             if the specified object already exist in the container
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo putObject(OioUrl url, Long size, InputStream data, Long version,
            Map<String, String> properties, RequestContext reqCtx) throws OioException;

    /**
     * Push an object into the OpenIO-SDS namespace.
     *
     * @param url
     *            the URL of the object to create
     * @param reqCtx
     *            Common parameters to all requests
     * @param size
     *            the size of the object
     * @param data
     *            the file to read the data from
     * @param version
     *            the version of content to upload
     * @param properties
     *            the properties to set to the object. Note that the properties
     *            are case insensitive and will always be returned in lower
     *            case.
     * @return information about the uploaded object
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo putObject(OioUrl url, Long size, File data, Long version,
            Map<String, String> properties, RequestContext reqCtx) throws OioException;

    /**
     * Returns information about the specified object
     * 
     * @param url
     *            the url of the object
     * @return an {@code ObjectInfo}
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo getObjectInfo(OioUrl url) throws OioException;

    /**
     * Returns information about the specified object
     *
     * @param url
     *            the url of the object
     * @param loadProperties
     *            if true, additional request is performed to get object
     *            properties assigned to the specified object
     * @return an {@code ObjectInfo}
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo getObjectInfo(OioUrl url, boolean loadProperties) throws OioException;

    /**
     * Returns information about the specified object
     * 
     * @param url
     *            the url of the object
     * @param version
     *            the version to get (could be {@code null} to get latest
     *            version)
     * @return an {@code ObjectInfo}
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo getObjectInfo(OioUrl url, Long version) throws OioException;

    /**
     * Returns information about the specified object
     *
     * @param url
     *            the url of the object
     * @param version
     *            the version to get (could be {@code null} to get latest
     *            version)
     * @param loadProperties
     *            if true, additional request is performed to get object
     *            properties assigned to the specified object
     * @return an {@code ObjectInfo}
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo getObjectInfo(OioUrl url, Long version, boolean loadProperties)
            throws OioException;

    /**
     * Returns information about the specified object
     *
     * @param url
     *            the URL of the object
     * @param version
     *            the version to get (could be {@code null} to get latest
     *            version)
     * @param loadProperties
     *            if true, an additional request is performed to get object
     *            properties assigned to the specified object
     * @param reqCtx
     *            common parameters to all requests
     * @return an {@code ObjectInfo}
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public ObjectInfo getObjectInfo(OioUrl url, Long version, boolean loadProperties,
            RequestContext reqCtx) throws OioException;

    /**
     * Returns the data of the specified object
     * 
     * @param oinf
     *            the information about object to download
     * @return the data in InputStream format
     * 
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public InputStream downloadObject(ObjectInfo oinf) throws OioException;

    /**
     * Get object data from a specified range.
     * 
     * @param oinf
     *            the information about object to download
     * @param range
     *            the wanted data range
     * 
     * @return the data in {@link InputStream} format
     * 
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public InputStream downloadObject(ObjectInfo oinf, Range range);

    /**
     * Get object data from a specified range.
     *
     * @param oinf
     *            the information about object to download
     * @param range
     *            the wanted data range
     * @param reqCtx
     *            common parameters to all requests
     *
     * @return the data in InputStream format
     *
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public InputStream downloadObject(ObjectInfo oinf, Range range, RequestContext reqCtx);

    /**
     * Deletes the specified object
     * 
     * @param url
     *            the url of the object to delete
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     * 
     */
    public void deleteObject(OioUrl url) throws OioException;

    /**
     * Delete the specified object.
     *
     * @param url
     *            the URL of the object to delete
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void deleteObject(OioUrl url, RequestContext reqCtx) throws OioException;

    /**
     * Deletes the specified object
     * 
     * @param url
     *            the url of the object to delete
     * @param version
     *            the version to delete (could be {@code null} to delete latest
     *            version)
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     * 
     */
    public void deleteObject(OioUrl url, Long version) throws OioException;

    /**
     * Delete the specified object.
     *
     * @param url
     *            the URL of the object to delete
     * @param version
     *            the version to delete (could be {@code null} to delete latest
     *            version)
     * @param reqCtx
     *            common parameters to all requests
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void deleteObject(OioUrl url, Long version, RequestContext reqCtx) throws OioException;

    /**
     * Add properties to the specified object. The properties must be prefixed
     * with "user." and this prefix will be stored, and finally used to query
     * the parameters later.
     * 
     * @param url
     *            the url of the object
     * @param props
     *            the properties to set
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void setObjectProperties(OioUrl url, Map<String, String> props) throws OioException;

    /**
     * Add properties to the specified object. The properties must be prefixed
     * with "user." and this prefix will be stored, and finally used to query
     * the parameters later.
     *
     * @param url
     *            the URL of the object
     * @param props
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
    public void setObjectProperties(OioUrl url, Map<String, String> props, RequestContext reqCtx)
            throws OioException;

    /**
     * Retrieves user properties of the specified object
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
    public Map<String, String> getObjectProperties(OioUrl url) throws OioException;

    /**
     * Retrieve user properties of the specified object.
     *
     * @param url
     *            the URL of the object
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
    public Map<String, String> getObjectProperties(OioUrl url, RequestContext reqCtx)
            throws OioException;

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
    public void deleteObjectProperties(OioUrl url, String... keys) throws OioException;

    /**
     * Deletes the specified properties from the object
     *
     * @param url
     *            the url of the object
     * @param reqCtx
     *            common parameters to all requests
     * @param keys
     *            the property keys to drop
     * @throws ContainerNotFoundException
     *             if the specified container doesn't exist
     * @throws ObjectNotFoundException
     *             if the specified object doesn't exist
     * @throws OioSystemException
     *             if any error occurs during request execution
     */
    public void deleteObjectProperties(OioUrl url, RequestContext reqCtx, String... keys)
            throws OioException;

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
    public void deleteObjectProperties(OioUrl url, List<String> keys) throws OioException;

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
    public void deleteObjectProperties(OioUrl url, List<String> keys, RequestContext reqCtx)
            throws OioException;
}
