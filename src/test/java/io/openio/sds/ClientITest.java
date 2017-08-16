package io.openio.sds;

import static io.openio.sds.TestHelper.testAccount;
import static io.openio.sds.common.OioConstants.OIO_CHARSET;
import static io.openio.sds.models.OioUrl.url;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import io.openio.sds.common.Hex;
import io.openio.sds.exceptions.BadRequestException;
import io.openio.sds.exceptions.ContainerExistException;
import io.openio.sds.exceptions.ContainerNotFoundException;
import io.openio.sds.exceptions.ObjectNotFoundException;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.OioUrl;
import io.openio.sds.models.Range;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ClientITest {

    private static Client client;
    public static final int httpReadTimeout = 10000;

    @BeforeClass
    public static void setup() {
        Settings settings = new Settings();
        settings.proxy().ns(TestHelper.ns()).url(TestHelper.proxyd())
                .ecd(TestHelper.ecd());
        settings.rawx().http().readTimeout(httpReadTimeout);
        client = ClientBuilder.newClient(settings);
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
        client.deleteContainer(url(testAccount(), UUID.randomUUID().toString()));
    }

    @Test(expected = ContainerNotFoundException.class)
    public void unknownContainerInfo() {
        client.getContainerInfo(url(testAccount(), UUID.randomUUID().toString()));
    }

    @Test
    public void handleEmptyObject() throws IOException,
            NoSuchAlgorithmException {
        OioUrl url = url(testAccount(), UUID.randomUUID().toString(), UUID
                .randomUUID().toString());
        client.createContainer(url);
        try {
            client.putObject(url, 0L,
                    new ByteArrayInputStream("".getBytes(OIO_CHARSET)));
            try {
                ObjectInfo oinf = client.getObjectInfo(url);
                Assert.assertNotNull(oinf);
                InputStream in = client.downloadObject(oinf);
                Assert.assertEquals(-1, in.read());
                Assert.assertEquals(
                        Hex.toHex(MessageDigest.getInstance("MD5").digest()),
                        oinf.hash());
                in.close();
            } finally {
                client.deleteObject(url);
            }
        } catch (BadRequestException exc) {
            throw exc;
        } finally {
            client.deleteContainer(url);
        }
    }

    @Test
    public void handleSizedObject() throws IOException,
            NoSuchAlgorithmException {
        byte[] src = TestHelper.bytes(1024L);
        OioUrl url = url(testAccount(), UUID.randomUUID().toString(), UUID
                .randomUUID().toString());
        client.createContainer(url);
        try {
            client.putObject(url, 1024L, new ByteArrayInputStream(src));
            try {
                ObjectInfo oinf = client.getObjectInfo(url);
                Assert.assertNotNull(oinf);
                Assert.assertEquals(1024, oinf.size().longValue());
                Assert.assertTrue(0 < oinf.ctime());
                Assert.assertNotNull(oinf.policy());
                Assert.assertNotNull(oinf.chunkMethod());
                Assert.assertNotNull(oinf.hashMethod());
                checkObject(oinf, new ByteArrayInputStream(src));
                Assert.assertEquals(
                        Hex.toHex(MessageDigest.getInstance("MD5").digest(src)),
                        oinf.hash());
            } finally {
                client.deleteObject(url);
            }
        } finally {
            client.deleteContainer(url);
        }

    }

    @Test
    public void range() throws IOException, NoSuchAlgorithmException {
        byte[] src = TestHelper.bytes(1024L);
        OioUrl url = url(testAccount(), UUID.randomUUID().toString(), UUID
                .randomUUID().toString());
        client.createContainer(url);

        byte[] ranged = Arrays.copyOfRange(src, 10, 20);

        try {
            client.putObject(url, 1024L, new ByteArrayInputStream(src));
            try {
                ObjectInfo oinf = client.getObjectInfo(url);
                Assert.assertNotNull(oinf);
                Assert.assertEquals(1024, oinf.size().longValue());
                Assert.assertTrue(0 < oinf.ctime());
                Assert.assertNotNull(oinf.policy());
                Assert.assertNotNull(oinf.chunkMethod());
                Assert.assertNotNull(oinf.hashMethod());
                checkObject(oinf, Range.between(10, 20),
                        new ByteArrayInputStream(ranged));
                Assert.assertEquals(
                        Hex.toHex(MessageDigest.getInstance("MD5").digest(src)),
                        oinf.hash());
            } finally {
                client.deleteObject(url);
            }
        } finally {
            client.deleteContainer(url);
        }

    }

    @Test
    public void handleMultiChunkObject() throws IOException,
            NoSuchAlgorithmException {
        byte[] src = TestHelper.bytes(10 * 1000 * 1024L);
        OioUrl url = url(testAccount(), UUID.randomUUID().toString(), UUID
                .randomUUID().toString());

        client.createContainer(url);
        try {
            client.putObject(url, 10 * 1000 * 1024L, new ByteArrayInputStream(
                    src));
            try {
                ObjectInfo oinf = client.getObjectInfo(url);
                Assert.assertNotNull(oinf);
                checkObject(oinf, new ByteArrayInputStream(src));
                Assert.assertEquals(
                        Hex.toHex(MessageDigest.getInstance("MD5").digest(src)),
                        oinf.hash());
            } finally {
                // client.deleteObject(url);
                System.out.println(url);
            }
        } finally {
            // client.deleteContainer(url);
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteUnknownObject() {
        OioUrl url = url(testAccount(), UUID.randomUUID().toString(), UUID
                .randomUUID().toString());
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
        props.put("key1", "value1");
        props.put("key2", "value2");
        props.put("key3", "value3");
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
            client.deleteContainerProperties(url, "key1");
            res = client.getContainerProperties(url);
            assertNotNull(res);
            assertEquals(2, res.size());
        } finally {
            client.deleteContainer(url);
        }
    }

    @Test
    public void objectProperties() {
        OioUrl url = url("TEST", UUID.randomUUID().toString(), UUID
                .randomUUID().toString());
        Map<String, String> props = new HashMap<String, String>();
        props.put("user.key1", "value1");
        props.put("user.key2", "value2");
        props.put("user.key3", "value3");
        client.createContainer(url);
        try {
            client.putObject(
                    url,
                    10L,
                    new ByteArrayInputStream("0123456789".getBytes(OIO_CHARSET)));
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

    @Test
    public void objectPutAndGetWithProperties() {
        OioUrl url = url("TEST", UUID.randomUUID().toString(), UUID
                .randomUUID().toString());
        Map<String, String> props = new HashMap<String, String>();
        props.put("key1", "val1");
        client.createContainer(url);
        try {
            client.putObject(url, 10L,
                    new ByteArrayInputStream("0123456789".getBytes()), props);
            try {
                ObjectInfo oinf = client.getObjectInfo(url);
                Assert.assertNotNull(oinf);
                Assert.assertNotNull(oinf.oid());
                Assert.assertEquals(10, oinf.size().longValue());
                Assert.assertTrue(0 < oinf.ctime());
                Assert.assertNotNull(oinf.policy());
                Assert.assertNotNull(oinf.chunkMethod());
                Assert.assertNotNull(oinf.hashMethod());
                Assert.assertNotNull(oinf.properties());
                Assert.assertEquals(1, oinf.properties().size());
                Assert.assertTrue(oinf.properties().containsKey("key1"));
                Assert.assertEquals("val1", oinf.properties().get("key1"));
            } finally {
                try {
                    client.deleteObject(url);
                } catch (Exception e) {
                }
            }

        } finally {
            try {
                client.deleteContainer(url);
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void objectPutShorterInput() {
        OioUrl url = url("TEST", UUID.randomUUID().toString(), UUID
                .randomUUID().toString());
        client.createContainer(url);
        ByteArrayInputStream bis = new ByteArrayInputStream("0123456789".getBytes());
        MissingByteInputStream mbis = new MissingByteInputStream(bis, 9, 0, httpReadTimeout + 1000);
        try {
            client.putObject(url, 10L, mbis);
            try {
                client.deleteObject(url);
            } catch (Exception e) {
            }
        } catch (OioException e) {
            if (e.getMessage().contains("jobs cancelled")) {
                System.out.println("Some upload workers force killed -> probable infinite loop");
                throw e;
            }
        } finally {
            try {
                client.deleteContainer(url);
            } catch (Exception e) {
            }
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
        int count = 0;
        for (int i = 0; i < oinf.size(); i++)
            try {
                // Assert.assertTrue(-1 != src.read());
                Assert.assertEquals("Fail at index: " + count, src.read(),
                        res[i] & 0xFF);
                count++;
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
    }

    private void checkObject(ObjectInfo oinf, Range range, InputStream src)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream in = client.downloadObject(oinf, range);
        byte[] buf = new byte[8192];
        int nbRead = 0;
        while (-1 < (nbRead = in.read(buf))) {
            if (0 < nbRead)
                bos.write(buf, 0, nbRead);
        }
        byte[] res = bos.toByteArray();
        int count = 0;
        for (int i = 0; i < range.to() - range.from(); i++)
            try {
                // Assert.assertTrue(-1 != src.read());
                Assert.assertEquals("Fail at index: " + count, src.read(),
                        res[i] & 0xFF);
                count++;
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
    }
}
