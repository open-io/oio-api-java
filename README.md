OpenIO SDS API
==============

OpenIO SDS API is a java remote API for [OpenIO Software Defined Storage](https://github.com/open-io/oio-sds). It is designed
 to be as simple and fast as possible.

-------------
### Getting started
------------

###### OioUrl object

  Each methods of the api will ask you for an OioUrl instance. This class simplify methods signatures and make code cleaner.
  You will build it simply by calling OioUrl#url(String, String) for container url, i.e. to perform operations on container,
  and by calling OioUrl#url(String, String, String) for object url. 
  
  The first parameter is called "ACCOUNT". It define a storage space inside your SDS namespace. It could be used 
  for a dedicated application space for example. If you don't care about that, just choose an account name and keep the same all the time.
  
  The second parameter is the container name. It is the space in which your future objects will be created directly. 
  Its name is unique by account.
  
  And the last parameter is the object name, which is the identifier of your object inside a container, so, like container in account,
  an object name is unique inside a container.
  
  Container url example:
     
    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME");
    
  Object url example:
  
    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME", "MY_OBJECT_NAME");

------------
### Storage client usage
------------

  OpenIO SDS API entry point is the Client implementations classes. Instances of these implementations
  are built with ClientBuilder class. You could create a basic client by calling
  ClientBuilder#newClient(String, String)} method, specifying the OpenIO namespace and proxyd
  service url as argument. If you don't know what is proxyd service, please refer to OpenIO SDS documentation [here](https://github.com/open-io/oio-sds/wiki/OpenIO-SDS-Proxy).
  Clients built from ClientBuilder are ready to be used. Let's see some basics examples.

###### Basic client instantiation
   
    Client client = ClientBuilder.newClient("OPENIO", "http://127.0.0.1:6002");

###### Advanced client configuration

    Settings settings = new Settings();
        settings.proxy()
                .ns("OPENIO")
                .url("http://127.0.0.1:6002")
                .ecd("http://127.0.0.1:5000"); //setup an ecd url for Erasure cogin management
        client = ClientBuilder.newClient(settings);
    

###### Container creation example 

As simple as it could be:

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME");
    client.createContainer(url);
    
###### Upload an object from an InputStream

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME", "MY_OBJECT_NAME");
    File file = new File("MY_SAMPLE_FILE.txt");
    FileInputStream fis = new FileInputStream(file);
    client.putObject(url, 1024L, fis);

###### Retrieve and download an object

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME", "MY_OBJECT_NAME");
    ObjectInfo oinf = client.getObjectInfo(url);
    InputStream data = client.downloadObject(oinf);
    
###### List objects inside a container

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME");
    ObjectList list = client.listContainer(url, new ListOptions());
            
###### Delete a content

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME", "MY_OBJECT_NAME");
    client.deleteObject(url);

###### Delete a container

An empty container (it should be explicitly empty) could be deleted from your SDS namespace, as follow.

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME");
    client.deleteContainer(url);
    
------------
### Exception management
------------

OpenIO choose to make all possible exceptions as RuntimeException extensions. By this way, we allow cleaner code 
when you decide to not check exceptions because you can't deal with unexpected behaviour (IOException etc...).
So our client throws only OioException : OioSystemException which indicates server error (connection issues, hardware issues,...),
and others OioException extension for all use case errors. See io.openio.sds.exceptions package for exceptions detail.

Usage exceptions are detailed just below

| Name                              | Description                                        |
| --------------------------------- | -------------------------------------------------- |
| AccountNotFoundException          | The account you try to deal with does not exist    |
| BadRequestException               | Some parameter is wrong in your request            |
| ContainerExistException           | The container you try to create is already created |
| ContainerNotFoundException        | The container you want to reach does not exist     | 
| ObjectExistException              | The object you try to created is alreay created    |
| ObjectNotFoundException           | The object you try to reach does not exist         |
| OioException                      | Main OpenIO exception class                        |
| OioSystemException                | A system error occurred during your request        |
| ReferenceAlreadyExistException    | The reference you try to create is alreayd created |
| ReferenceNotFoundException        | the reference you try to reach does not exist      |