package io.openio.sds;

import static io.openio.sds.common.OioConstants.OIO_CHARSET;
import static io.openio.sds.models.OioUrl.url;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.openio.sds.exceptions.ContainerExistException;
import io.openio.sds.exceptions.ContainerNotFoundException;
import io.openio.sds.exceptions.ObjectNotFoundException;
import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.OioUrl;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ClientTest {

    private static final String TEST_ACCOUNT = "TEST";

    private static Client client;
    private static TestHelper helper = TestHelper.instance();

    @BeforeClass
    public static void setup() {
        client = ClientBuilder.newClient(helper.ns(), helper.proxyd());
    }

    @AfterClass
    public static void teardown() {
    }

    @Test
    public void handleContainer() {
        OioUrl url = url(TEST_ACCOUNT, UUID.randomUUID().toString());
        client.createContainer(url);
        ContainerInfo ci = client.getContainerInfo(url);
        assertNotNull(ci);
        client.deleteContainer(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createContainerNullUrl() {
        client.createContainer(null);
    }

    @Test(expected = ContainerExistException.class)
    public void doubleCreateContainer() {
        OioUrl url = url(TEST_ACCOUNT, UUID.randomUUID().toString());
        client.createContainer(url);
        try {
            client.createContainer(url);
        } finally {
            client.deleteContainer(url);
        }
    }

    @Test(expected = ContainerNotFoundException.class)
    public void deleteUnknownContainer() {
        client.deleteContainer(url(TEST_ACCOUNT, UUID.randomUUID().toString()));
    }

    @Test(expected = ContainerNotFoundException.class)
    public void unknownContainerInfo() {
        client.getContainerInfo(
                url(TEST_ACCOUNT, UUID.randomUUID().toString()));
    }

    @Test
    public void handleEmptyObject() throws IOException {
        OioUrl url = url(TEST_ACCOUNT,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        client.createContainer(url);
        try {
            client.putObject(url, 0L,
                    new ByteArrayInputStream("".getBytes(OIO_CHARSET)));
            try {
                ObjectInfo oinf = client.getObjectInfo(url);
                Assert.assertNotNull(oinf);
                InputStream in = client.downloadObject(oinf);
                Assert.assertEquals(-1, in.read());
                in.close();
            } finally {
                client.deleteObject(url);
            }
        } finally {
            client.deleteContainer(url);
        }

    }

    @Test
    public void handleSizedObject() throws IOException {
        byte[] src = TestHelper.bytes(1024L);
        OioUrl url = url(TEST_ACCOUNT,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        client.createContainer(url);
        try {
            client.putObject(url, 1024L,
                    new ByteArrayInputStream(src));
            try {
                ObjectInfo oinf = client.getObjectInfo(url);
                Assert.assertNotNull(oinf);
                checkObject(oinf, new ByteArrayInputStream(src));
            } finally {
                client.deleteObject(url);
            }
        } finally {
            client.deleteContainer(url);
        }

    }
    
    @Test
    public void handleMultiChunkObject() throws IOException {
        byte[] src = TestHelper.bytes(1090000L);
        OioUrl url = url(TEST_ACCOUNT,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        client.createContainer(url);
        try {
            client.putObject(url, 1090000L,
                    new ByteArrayInputStream(src));
            try {
                ObjectInfo oinf = client.getObjectInfo(url);
                Assert.assertNotNull(oinf);
                checkObject(oinf, new ByteArrayInputStream(src));
            } finally {
                client.deleteObject(url);
            }
        } finally {
            client.deleteContainer(url);
        }

    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteUnknownObject() {
        OioUrl url = url(TEST_ACCOUNT,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        client.createContainer(url);
        try {
            client.deleteObject(url);
        } finally {
            client.deleteContainer(url);
        }
    }

    private void checkObject(ObjectInfo oinf, InputStream src)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream in = client.downloadObject(oinf);
        byte[] buf = new byte[8192];
        int nbRead = 0;
        while (-1 < (nbRead = in.read(buf))) {
            if (0 < nbRead)
                bos.write(buf, 0, nbRead);
        }
        byte[] res = bos.toByteArray();
        for (int i = 0; i < oinf.size(); i++)
            try {
                Assert.assertEquals(src.read(), res[i] & 0xFF);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
    }
}
