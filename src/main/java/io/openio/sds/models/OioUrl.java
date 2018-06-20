package io.openio.sds.models;

import static io.openio.sds.common.Check.checkArgument;
import static io.openio.sds.common.OioConstants.OIO_CHARSET;
import static io.openio.sds.common.Strings.nullOrEmpty;
import io.openio.sds.common.Hash;
import io.openio.sds.common.MoreObjects;
import io.openio.sds.common.Strings;

/**
 * 
 *
 *
 */
public class OioUrl {

    private static final byte[] BACK_ZERO = { '\0' };

    private String ns;
    private String account;
    private String container;
    private String cid;
    private String object;

    private OioUrl(String ns, String account, String container, String cid,
            String object) {
        this.ns = ns;
        this.account = account;
        this.container = container;
        this.cid = cid;
        this.object = object;
    }
    
    
    
    public static OioUrl url(String account, String container) {
        return url(account, container, null);
    }

    public static OioUrl url(String account, String container,
            String object) {
        checkArgument(!nullOrEmpty(account),
                "account cannot be null or empty");
        checkArgument(!nullOrEmpty(container),
                "container cannot be null or empty");
        return new OioUrl(null,
                account,
                container,
                cid(account, container),
                object);
    }
    
    public static OioUrl url(String ns, String account, String container,
            String object) {
        checkArgument(!nullOrEmpty(account),
                "account cannot be null or empty");
        checkArgument(!nullOrEmpty(container),
                "container cannot be null or empty");
        return new OioUrl(ns, 
                account,
                container,
                cid(account, container),
                object);
    }

    public String namespace() {
        return ns;
    }

    public OioUrl namespace(String namespace) {
        this.ns = namespace;
        return this;
    }

    public String account() {
        return account;
    }

    public OioUrl account(String account) {
        this.account = account;
        return this;
    }

    public String container() {
        return container;
    }

    public OioUrl container(String container) {
        this.container = container;
        return this;
    }

    public String object() {
        return object;
    }

    public OioUrl object(String object) {
        this.object = object;
        return this;
    }

    public String cid() {
        return cid;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("namespace", ns)
                .add("account", account)
                .add("container", container)
                .add("object", object)
                .toString();
    }

    /**
     * Generates the container id from the specified account and container name
     * 
     * @param account
     *            the name of the account
     * @param container
     *            the name of the container
     * @return the generated id
     */
    public static String cid(String account, String container) {
        return Hash.sha256()
                .putBytes(account.getBytes(OIO_CHARSET))
                .putBytes(BACK_ZERO)
                .putBytes(container.getBytes(OIO_CHARSET))
                .hash().toString();
    }

}