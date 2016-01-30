package io.openio.sds;

import java.io.File;
import java.io.InputStream;

import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.ObjectList;
import io.openio.sds.models.OioUrl;

/**
 * <p>
 * Contains all convenience methods to handle containers and objects in OpenIO
 * SDS
 * </p>
 * <p>
 * Instances of {@code Client} implementations are built with
 * {@link ClientBuilder} class. You could create a basic client by calling
 * {@link ClientBuilder#newClient(String)} method, specifying the OpenIO proxyd
 * service url as argument. The built client is ready to use.
 * </p>
 * <p>
 * Simple example:
 * 
 * <code>
 * Client client = ClientBuilder.newClient("http://127.0.0.1:6002");
 * </code>
 * 
 * </p>
 * <p>
 * To enable more tunable client instance, you should specify some directives to
 * your {@link ClientBuilder} For example if you want to provide your own
 * AsyncHttpClient instance, you have to proceed as follow:
 * 
 * <code>
 * AsyncHttpClient http = Dsl.asyncHttpClient(
 *       new DefaultAsyncHttpClientConfig.Builder()
 *             .setRequestTimeout(10000)
 *             .setHttpClientCodecMaxChunkSize(8192 * 4));
 * Client client = ClientBuilder.prepareClient()
 *       .proxydUrl("http://127.0.0.1:6002")
 *       .http(http)
 *       .build();
 * </code>
 * </p>
 * 
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public interface Client {

    /**
     * Creates a container using the specified {@link OioUrl}. OioUrl are built
     * by using {@link OioUrl#url(String, String)} method, then you have to
     * specify the name of the account to use and the name of the future
     * container.
     * <p>
     * The container is available when the returned future is completed.
     * 
     * @param url
     *            the url of the container
     * @return a {@code ContainerInfo} with informations about the created
     *         container
     */
    public ContainerInfo createContainer(OioUrl url);

    /**
     * Returns informations about the specified container
     * 
     * @param url
     *            the url of the container
     * @return a {@code ContainerInfo} with informations about the created
     *         container
     */
    public ContainerInfo getContainerInfo(OioUrl url);

    /**
     * List object available in the specified container
     * 
     * @param url
     *            the url of the container
     * @param listOptions
     *            the listing option
     * 
     * @return the ObjectList
     */
    public ObjectList listContainer(OioUrl url,
            final ListOptions listOptions);

    /**
     * Deletes the specified container
     * 
     * @param url
     *            the url of the container
     */
    public void deleteContainer(OioUrl url);

    /**
     * Push an object into the oio namespace
     * 
     * @param OioUrl
     *            the url of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the file to read the data from
     * @return informations about the uploaded object
     * 
     */
    public ObjectInfo putObject(OioUrl url, long size, File data);

    /**
     * Push an object into the oio namespace
     * 
     * @param OioUrl
     *            the url of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the InputStream to read the data from
     * @return informations about the uploaded object
     */
    public ObjectInfo putObject(OioUrl url, long size,
            InputStream data);

    /**
     * Returns informations about the specified object
     * 
     * @param url
     *            the url of the object
     * @return an {@code ObjectInfo}
     */
    public ObjectInfo getObjectInfo(OioUrl url);

    /**
     * Returns the data of the specified object
     * 
     * @param oinf
     *            the informations about object to download
     * @return the data in InputStream format
     */
    public InputStream downloadObject(ObjectInfo oinf);

    /**
     * Deletes the specified object
     * 
     * @param url
     *            the url of the object to delete
     * 
     */
    public void deleteObject(OioUrl url);
}