package io.openio.sds;

import static io.openio.sds.TestHelper.ns;
import static io.openio.sds.TestHelper.proxyd;
import static io.openio.sds.TestHelper.testAccount;
import static io.openio.sds.common.OioConstants.OIO_CHARSET;
import static io.openio.sds.models.OioUrl.url;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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

    private static Client client;

    @BeforeClass
    public static void setup() {
        client = ClientBuilder.newClient(ns(), proxyd());
    }

    @AfterClass
    public static void teardown() {
    }

    @Test
    public void handleContainer() {
        OioUrl url = url(testAccount(), UUID.randomUUID().toString());
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
        OioUrl url = url(testAccount(), UUID.randomUUID().toString());
        client.createContainer(url);
        try {
            client.createContainer(url);
        } finally {
            client.deleteContainer(url);
        }
    }

    @Test(expected = ContainerNotFoundException.class)
    public void deleteUnknownContainer() {
        client.deleteContainer(
                url(testAccount(), UUID.randomUUID().toString()));
    }

    @Test(expected = ContainerNotFoundException.class)
    public void unknownContainerInfo() {
        client.getContainerInfo(
                url(testAccount(), UUID.randomUUID().toString()));
    }

    @Test
    public void handleEmptyObject() throws IOException {
        OioUrl url = url(testAccount(),
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
        OioUrl url = url(testAccount(),
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
        OioUrl url = url(testAccount(),
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
        OioUrl url = url(testAccount(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        client.createContainer(url);
        try {
            client.deleteObject(url);
        } finally {
            client.deleteContainer(url);
        }
    }

    @Test
    public void containerProperties() {
        OioUrl url = url("TEST", UUID.randomUUID().toString());
        Map<String, String> props = new HashMap<String, String>();
        props.put("user.key1", "value1");
        props.put("user.key2", "value2");
        props.put("user.key3", "value3");
        client.createContainer(url);
        try {
            client.setContainerProperties(url, props);
            Map<String, String> res = client.getContainerProperties(url);
            assertNotNull(res);
            assertEquals(3, res.size());
            for (Entry<String, String> e : props.entrySet()) {
                assertTrue(res.containsKey(e.getKey()));
                assertEquals(e.getValue(), res.get(e.getKey()));
            }
            client.deleteContainerProperties(url, "user.key1");
            res = client.getContainerProperties(url);
            assertNotNull(res);
            assertEquals(2, res.size());
        } finally {
            client.deleteContainer(url);
        }
    }

    @Test
    public void objectProperties() {
        OioUrl url = url("TEST", UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        Map<String, String> props = new HashMap<String, String>();
        props.put("user.key1", "value1");
        props.put("user.key2", "value2");
        props.put("user.key3", "value3");
        client.createContainer(url);
        try {
            client.putObject(url, 10L, new ByteArrayInputStream(
                    "0123456789".getBytes(OIO_CHARSET)));
            try {
                client.setObjectProperties(url, props);
                Map<String, String> res = client.getObjectProperties(url);
                assertNotNull(res);
                assertEquals(3, res.size());
                for (Entry<String, String> e : props.entrySet()) {
                    assertTrue(res.containsKey(e.getKey()));
                    assertEquals(e.getValue(), res.get(e.getKey()));
                }
                client.deleteObjectProperties(url, "user.key1");
                res = client.getObjectProperties(url);
                assertNotNull(res);
                assertEquals(2, res.size());
                // check props are set on object as expected and not on
                // container
                assertEquals(0, client.getContainerProperties(url).size());
            } finally {
                client.deleteObject(url);
            }
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
